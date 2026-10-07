package com.example.dispatch.planner;

import com.example.dispatch.ai.AiProvider;
import com.example.dispatch.constraint.ConstraintValidator;
import com.example.dispatch.constraint.ValidationResult;
import com.example.dispatch.exception.AiPlanningException;
import com.example.dispatch.entity.Assignment;
import com.example.dispatch.entity.ServiceRequest;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.enums.RequestStatus;
import com.example.dispatch.repository.AssignmentRepository;
import com.example.dispatch.repository.ServiceRequestRepository;
import com.example.dispatch.repository.TechnicianRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlannerService {

    private final ConstraintValidator constraintValidator;
    private final ServiceRequestRepository requestRepository;
    private final TechnicianRepository technicianRepository;
    private final AssignmentRepository assignmentRepository;
    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

    public PlannerService(ConstraintValidator constraintValidator,
                          ServiceRequestRepository requestRepository,
                          TechnicianRepository technicianRepository,
                          AssignmentRepository assignmentRepository,
                          AiProvider aiProvider) {
        this.constraintValidator = constraintValidator;
        this.requestRepository = requestRepository;
        this.technicianRepository = technicianRepository;
        this.assignmentRepository = assignmentRepository;
        this.aiProvider = aiProvider;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Transactional(readOnly = true)
    public ScheduleProposal generatePlan(String date) {
        List<ServiceRequest> pendingRequests = requestRepository.findAll().stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());
        List<Technician> allTechnicians = technicianRepository.findAll();
        List<Assignment> currentConfirmedAssignments = assignmentRepository.findAll();

        // 1. Construct Prompt
        String systemPrompt = """
            You are an expert Field Service Dispatch AI.
            Your task is to assign Service Requests to Technicians.
            You must output raw JSON strictly conforming to this schema:
            {
              "assignments": [
                 { "requestId": number, "technicianId": number, "startTime": "HH:MM:00", "endTime": "HH:MM:00" }
              ],
              "unassignedRequests": [
                 { "requestId": number, "reason": "string" }
              ],
              "tradeoffs": ["string"],
              "risks": ["string"]
            }
            Rules:
            - Technicians can only be assigned if they have the required skill.
            - Do not double book a technician.
            - Emergency priority goes first.
            - Balance the workload.
            """;

        String userPrompt = "";
        try {
            userPrompt = String.format("Pending Requests: %s\n\nTechnicians: %s\n\nCurrent Schedule: %s",
                    objectMapper.writeValueAsString(pendingRequests),
                    objectMapper.writeValueAsString(allTechnicians),
                    objectMapper.writeValueAsString(currentConfirmedAssignments));
        } catch (Exception e) {
            throw new AiPlanningException("Failed to serialize state for AI", e);
        }

        // 2. Call AI Provider (Groq / Gemini / Mock)
        String jsonResponse = aiProvider.getCompletion(systemPrompt, userPrompt);

        // 3. Parse AI Response
        ScheduleProposal proposal;
        try {
            proposal = objectMapper.readValue(jsonResponse, ScheduleProposal.class);
        } catch (Exception e) {
            throw new AiPlanningException("AI returned malformed JSON: " + jsonResponse, e);
        }

        // 4. Safely validate every single AI suggestion (Deterministic Fallback)
        List<ProposedAssignment> validAssignments = new ArrayList<>();
        List<Assignment> simulatedState = new ArrayList<>(currentConfirmedAssignments);

        for (ProposedAssignment aiAssn : proposal.getAssignments()) {
            Technician tech = allTechnicians.stream().filter(t -> t.getId().equals(aiAssn.getTechnicianId())).findFirst().orElse(null);
            ServiceRequest req = pendingRequests.stream().filter(r -> r.getId().equals(aiAssn.getRequestId())).findFirst().orElse(null);
            
            if (tech == null || req == null) {
                proposal.getUnassignedRequests().add(new UnassignedReason(aiAssn.getRequestId(), "AI Hallucinated Invalid ID."));
                continue;
            }

            LocalTime start = aiAssn.getStartTime();
            LocalTime end = aiAssn.getEndTime();

            List<Assignment> techAssignments = simulatedState.stream()
                    .filter(a -> a.getTechnicianId().equals(tech.getId()))
                    .collect(Collectors.toList());

            ValidationResult result = constraintValidator.validateAssignment(tech, req, start, end, techAssignments);
            
            if (result.isValid()) {
                validAssignments.add(aiAssn);
                Assignment simAssn = new Assignment();
                simAssn.setRequestId(req.getId());
                simAssn.setTechnicianId(tech.getId());
                simAssn.setStartTime(start);
                simAssn.setEndTime(end);
                simulatedState.add(simAssn);
            } else {
                proposal.getUnassignedRequests().add(new UnassignedReason(
                        aiAssn.getRequestId(),
                        "AI suggestion rejected by safety validator: " + String.join(", ", result.getViolations())
                ));
            }
        }

        proposal.setAssignments(validAssignments);
        return proposal;
    }
}
