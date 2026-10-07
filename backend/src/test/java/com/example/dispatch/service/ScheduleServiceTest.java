package com.example.dispatch.service;

import com.example.dispatch.constants.ErrorCode;
import com.example.dispatch.constraint.ConstraintValidator;
import com.example.dispatch.entity.*;
import com.example.dispatch.enums.*;
import com.example.dispatch.planner.ProposedAssignment;
import com.example.dispatch.planner.ScheduleProposal;
import com.example.dispatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private ScheduleVersionRepository versionRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private ServiceRequestRepository requestRepository;
    @Mock private AuditLogRepository auditLogRepository;

    private ConstraintValidator validator = new ConstraintValidator();
    
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                scheduleRepository, versionRepository, assignmentRepository,
                technicianRepository, requestRepository, validator, auditLogRepository
        );
    }

    @Test
    void testConfirmSchedule_Success() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setCurrentVersion(1L);

        Technician technician = new Technician();
        technician.setId(10L);
        technician.setStatus(TechnicianStatus.AVAILABLE);
        technician.setSkills(java.util.List.of("PLUMBING"));
        technician.setAssignedRegion("NORTH");
        technician.setAvailabilityStart(LocalTime.of(9, 0));
        technician.setAvailabilityEnd(LocalTime.of(17, 0));
        technician.setMaximumWorkloadMinutes(480);

        ServiceRequest request = new ServiceRequest();
        request.setId(100L);
        request.setRequiredSkill("PLUMBING");
        request.setRegion("NORTH");

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(technicianRepository.findById(10L)).thenReturn(Optional.of(technician));
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        
        ScheduleVersion mockVersion = new ScheduleVersion();
        mockVersion.setId(5L);
        mockVersion.setVersionNumber(2);
        when(versionRepository.save(any(ScheduleVersion.class))).thenReturn(mockVersion);

        ScheduleProposal proposal = new ScheduleProposal();
        proposal.getAssignments().add(new ProposedAssignment(100L, 10L, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        ScheduleVersion createdVersion = scheduleService.confirmScheduleProposal(1L, 1L, proposal, "Dispatcher", ScheduleChangeReason.INITIAL_PLAN);

        assertNotNull(createdVersion);
        assertEquals(2, createdVersion.getVersionNumber());

        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(requestRepository, times(1)).save(request);
        verify(scheduleRepository, times(1)).save(schedule);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        
        assertEquals(RequestStatus.ASSIGNED, request.getStatus());
        assertEquals(2L, schedule.getCurrentVersion());
    }

    @Test
    void testConfirmSchedule_StaleSchedule() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setCurrentVersion(2L); // Database is on version 2

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        ScheduleProposal proposal = new ScheduleProposal();

        // UI passes expected version 1, but db is 2
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scheduleService.confirmScheduleProposal(1L, 1L, proposal, "Dispatcher", ScheduleChangeReason.MANUAL_EDIT);
        });

        assertEquals(ErrorCode.STALE_SCHEDULE, exception.getMessage());
        verify(versionRepository, never()).save(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void testConfirmSchedule_RevalidationFails() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setCurrentVersion(1L);

        Technician technician = new Technician();
        technician.setId(10L);
        technician.setStatus(TechnicianStatus.AVAILABLE);
        technician.setSkills(java.util.List.of("PLUMBING"));
        technician.setAssignedRegion("NORTH");
        technician.setAvailabilityStart(LocalTime.of(9, 0));
        technician.setAvailabilityEnd(LocalTime.of(17, 0));
        technician.setMaximumWorkloadMinutes(480);

        ServiceRequest request = new ServiceRequest();
        request.setId(100L);
        request.setRequiredSkill("ELECTRICAL"); // Hallucinated skill by AI! (tech only has plumbing)
        request.setRegion("NORTH");

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(technicianRepository.findById(10L)).thenReturn(Optional.of(technician));
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        
        when(versionRepository.save(any(ScheduleVersion.class))).thenReturn(new ScheduleVersion());

        ScheduleProposal proposal = new ScheduleProposal();
        proposal.getAssignments().add(new ProposedAssignment(100L, 10L, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            scheduleService.confirmScheduleProposal(1L, 1L, proposal, "Dispatcher", ScheduleChangeReason.INITIAL_PLAN);
        });

        assertTrue(exception.getMessage().contains(ErrorCode.ASSIGNMENT_CONFLICT));
        assertTrue(exception.getMessage().contains("skill"));
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }
}
