package com.example.dispatch.constraint;

import com.example.dispatch.entity.Assignment;
import com.example.dispatch.entity.ServiceRequest;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.enums.TechnicianStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintValidatorTest {

    private ConstraintValidator validator;
    private Technician technician;
    private ServiceRequest request;
    private List<Assignment> existingAssignments;

    @BeforeEach
    void setUp() {
        validator = new ConstraintValidator();

        technician = new Technician();
        technician.setId(1L);
        technician.setName("John Doe");
        technician.setStatus(TechnicianStatus.AVAILABLE);
        technician.setSkills(List.of("PLUMBING", "ELECTRICAL"));
        technician.setAssignedRegion("NORTH");
        technician.setAvailabilityStart(LocalTime.of(9, 0));
        technician.setAvailabilityEnd(LocalTime.of(17, 0));
        technician.setMaximumWorkloadMinutes(480);

        request = new ServiceRequest();
        request.setId(100L);
        request.setRequiredSkill("PLUMBING");
        request.setRegion("NORTH");
        request.setPreferredStartTime(LocalTime.of(10, 0));
        request.setPreferredEndTime(LocalTime.of(14, 0));

        existingAssignments = new ArrayList<>();
    }

    @Test
    void testValidAssignment() {
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(10, 0), LocalTime.of(12, 0), existingAssignments);
        assertTrue(result.isValid(), "Assignment should be valid");
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void testSkillMismatch() {
        request.setRequiredSkill("HVAC");
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(10, 0), LocalTime.of(12, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("skill")));
    }

    @Test
    void testRegionMismatch() {
        request.setRegion("SOUTH");
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(10, 0), LocalTime.of(12, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("region")));
    }

    @Test
    void testAvailabilityViolation() {
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(8, 0), LocalTime.of(10, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("available hours")));
    }

    @Test
    void testTimeWindowViolation() {
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(14, 0), LocalTime.of(16, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("preferred time window")));
    }

    @Test
    void testWorkloadExceeded() {
        Assignment longAssignment = new Assignment();
        longAssignment.setStartTime(LocalTime.of(9, 0));
        longAssignment.setEndTime(LocalTime.of(16, 0)); // 7 hours (420 mins)
        existingAssignments.add(longAssignment);

        // Try to add another 2 hours (120 mins) -> 540 mins > 480 mins max
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(16, 0), LocalTime.of(18, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("workload")));
    }

    @Test
    void testDoubleBookingOverlap() {
        Assignment existing = new Assignment();
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(12, 0));
        existingAssignments.add(existing);

        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(11, 0), LocalTime.of(13, 0), existingAssignments);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("overlaps")));
    }

    @Test
    void testBackToBackAssignmentsAllowed() {
        Assignment existing = new Assignment();
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setEndTime(LocalTime.of(11, 0));
        existingAssignments.add(existing);

        // Starts exactly when previous ends
        ValidationResult result = validator.validateAssignment(
                technician, request, LocalTime.of(11, 0), LocalTime.of(13, 0), existingAssignments);
        assertTrue(result.isValid(), "Back-to-back assignments should be valid");
    }
}
