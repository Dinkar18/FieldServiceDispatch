package com.example.dispatch.service;

import com.example.dispatch.constants.ErrorCode;
import com.example.dispatch.exception.ScheduleConflictException;
import com.example.dispatch.constraint.ConstraintValidator;
import com.example.dispatch.constraint.ValidationResult;
import com.example.dispatch.entity.*;
import com.example.dispatch.enums.*;
import com.example.dispatch.planner.ProposedAssignment;
import com.example.dispatch.planner.ScheduleProposal;
import com.example.dispatch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleVersionRepository versionRepository;
    private final AssignmentRepository assignmentRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceRequestRepository requestRepository;
    private final ConstraintValidator validator;
    private final AuditLogRepository auditLogRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           ScheduleVersionRepository versionRepository,
                           AssignmentRepository assignmentRepository,
                           TechnicianRepository technicianRepository,
                           ServiceRequestRepository requestRepository,
                           ConstraintValidator validator,
                           AuditLogRepository auditLogRepository) {
        this.scheduleRepository = scheduleRepository;
        this.versionRepository = versionRepository;
        this.assignmentRepository = assignmentRepository;
        this.technicianRepository = technicianRepository;
        this.requestRepository = requestRepository;
        this.validator = validator;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ScheduleVersion confirmScheduleProposal(Long scheduleId, Long expectedBaseVersion, ScheduleProposal proposal, String actor, ScheduleChangeReason reason) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.RESOURCE_NOT_FOUND));

        if (schedule.getCurrentVersion() != null && !schedule.getCurrentVersion().equals(expectedBaseVersion)) {
            throw new ScheduleConflictException(ErrorCode.STALE_SCHEDULE);
        }

        int nextVersionNum = 1;
        if (schedule.getCurrentVersion() != null) {
            ScheduleVersion prev = versionRepository.findById(schedule.getCurrentVersion()).orElse(null);
            if (prev != null) {
                nextVersionNum = prev.getVersionNumber() + 1;
            }
        }
        
        // Create new version
        ScheduleVersion newVersion = new ScheduleVersion();
        newVersion.setScheduleId(scheduleId);
        newVersion.setVersionNumber(nextVersionNum);
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setCreatedBy(actor);
        newVersion.setReason(reason);
        newVersion = versionRepository.save(newVersion);

        // Clone previous assignments to the new version
        List<Assignment> confirmedAssignments = new ArrayList<>();
        if (schedule.getCurrentVersion() != null) {
            List<Assignment> oldAssignments = assignmentRepository.findByScheduleVersionId(schedule.getCurrentVersion());
            for (Assignment oldAssn : oldAssignments) {
                if (oldAssn.getStatus() == AssignmentStatus.CONFIRMED) {
                    Assignment copied = new Assignment();
                    copied.setScheduleVersionId(newVersion.getId());
                    copied.setRequestId(oldAssn.getRequestId());
                    copied.setTechnicianId(oldAssn.getTechnicianId());
                    copied.setStartTime(oldAssn.getStartTime());
                    copied.setEndTime(oldAssn.getEndTime());
                    copied.setStatus(oldAssn.getStatus());
                    confirmedAssignments.add(assignmentRepository.save(copied));
                }
            }
        }

        for (ProposedAssignment pa : proposal.getAssignments()) {
            Technician tech = technicianRepository.findById(pa.getTechnicianId())
                    .orElseThrow(() -> new RuntimeException(ErrorCode.INVALID_TECHNICIAN));
            ServiceRequest req = requestRepository.findById(pa.getRequestId())
                    .orElseThrow(() -> new RuntimeException(ErrorCode.RESOURCE_NOT_FOUND));

            // Determine existing workload context
            List<Assignment> existingForTech = confirmedAssignments.stream()
                    .filter(a -> a.getTechnicianId().equals(tech.getId()))
                    .collect(Collectors.toList());

            // Re-validate against determinism
            ValidationResult vr = validator.validateAssignment(tech, req, pa.getStartTime(), pa.getEndTime(), existingForTech);
            if (!vr.isValid()) {
                throw new ScheduleConflictException(ErrorCode.ASSIGNMENT_CONFLICT + ": " + String.join(", ", vr.getViolations()));
            }

            Assignment assignment = new Assignment();
            assignment.setScheduleVersionId(newVersion.getId());
            assignment.setRequestId(req.getId());
            assignment.setTechnicianId(tech.getId());
            assignment.setStartTime(pa.getStartTime());
            assignment.setEndTime(pa.getEndTime());
            assignment.setStatus(AssignmentStatus.CONFIRMED);
            
            confirmedAssignments.add(assignmentRepository.save(assignment));
            
            // Update request status
            req.setStatus(RequestStatus.ASSIGNED);
            requestRepository.save(req);
        }

        schedule.setCurrentVersion(newVersion.getId());
        scheduleRepository.save(schedule);

        // Audit Log
        AuditLog audit = new AuditLog();
        audit.setAction("SCHEDULE_CONFIRMED");
        audit.setEntityType("SCHEDULE");
        audit.setEntityId(scheduleId);
        audit.setActor(actor);
        audit.setTimestamp(LocalDateTime.now());
        audit.setDetails("Confirmed version " + newVersion.getVersionNumber() + " with reason: " + reason);
        auditLogRepository.save(audit);

        return newVersion;
    }

    @Transactional
    public void cancelTechnicianAndUnassign(Long technicianId) {
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
        tech.setStatus(TechnicianStatus.CANCELLED);
        technicianRepository.save(tech);

        List<Assignment> techAssignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getTechnicianId().equals(technicianId) && a.getStatus() == AssignmentStatus.CONFIRMED)
                .collect(Collectors.toList());

        for (Assignment a : techAssignments) {
            a.setStatus(AssignmentStatus.CANCELLED);
            assignmentRepository.save(a);
            
            ServiceRequest req = requestRepository.findById(a.getRequestId()).orElse(null);
            if (req != null) {
                req.setStatus(RequestStatus.PENDING);
                requestRepository.save(req);
            }
        }
    }
}
