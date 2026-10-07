package com.example.dispatch.planner;

import com.example.dispatch.ai.AiProvider;
import com.example.dispatch.constraint.ConstraintValidator;
import com.example.dispatch.entity.Assignment;
import com.example.dispatch.entity.ServiceRequest;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.enums.Priority;
import com.example.dispatch.enums.RequestStatus;
import com.example.dispatch.enums.TechnicianStatus;
import com.example.dispatch.repository.AssignmentRepository;
import com.example.dispatch.repository.ServiceRequestRepository;
import com.example.dispatch.repository.TechnicianRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerServiceTest {

    private PlannerService plannerService;
    private ConstraintValidator validator;
    private ObjectMapper objectMapper;

    @Mock private ServiceRequestRepository requestRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AiProvider aiProvider;

    @BeforeEach
    void setUp() {
        validator = new ConstraintValidator();
        plannerService = new PlannerService(validator, requestRepository, technicianRepository, assignmentRepository, aiProvider);
    }

    @Test
    void testGeneratePlan_RejectsAiHallucinations() {
        Technician t1 = new Technician();
        t1.setId(1L);
        t1.setStatus(TechnicianStatus.AVAILABLE);
        t1.setSkills(List.of("PLUMBING"));
        t1.setAssignedRegion("NORTH");
        t1.setAvailabilityStart(LocalTime.of(9, 0));
        t1.setAvailabilityEnd(LocalTime.of(17, 0));
        t1.setMaximumWorkloadMinutes(480);

        ServiceRequest req = new ServiceRequest();
        req.setId(100L);
        req.setPriority(Priority.HIGH);
        req.setRequiredSkill("ELECTRICAL"); // Note: Tech 1 is PLUMBING
        req.setRegion("NORTH");
        req.setStatus(RequestStatus.PENDING);
        req.setEstimatedDurationMinutes(60);

        when(requestRepository.findAll()).thenReturn(List.of(req));
        when(technicianRepository.findAll()).thenReturn(List.of(t1));
        when(assignmentRepository.findAll()).thenReturn(new ArrayList<>());

        // AI maliciously or incorrectly hallucinates a bad assignment
        String badAiJson = """
        {
          "assignments": [
            { "requestId": 100, "technicianId": 1, "startTime": "10:00:00", "endTime": "11:00:00" }
          ],
          "unassignedRequests": [],
          "tradeoffs": [],
          "risks": []
        }
        """;
        when(aiProvider.getCompletion(anyString(), anyString())).thenReturn(badAiJson);

        ScheduleProposal proposal = plannerService.generatePlan("2026-10-07");

        // System should catch the lie
        assertEquals(0, proposal.getAssignments().size());
        assertEquals(1, proposal.getUnassignedRequests().size());
        assertTrue(proposal.getUnassignedRequests().get(0).getReason().contains("AI suggestion rejected"));
    }
}
