package com.example.dispatch.constraint;

import com.example.dispatch.entity.Assignment;
import com.example.dispatch.entity.ServiceRequest;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.enums.TechnicianStatus;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class ConstraintValidator {

    public ValidationResult validateAssignment(Technician technician, ServiceRequest request, LocalTime startTime, LocalTime endTime, List<Assignment> existingAssignments) {
        ValidationResult result = new ValidationResult();

        if (technician.getStatus() != TechnicianStatus.AVAILABLE) {
            result.addViolation("Technician is not available.");
        }

        if (!technician.getSkills().contains(request.getRequiredSkill())) {
            result.addViolation("Technician does not have the required skill: " + request.getRequiredSkill());
        }

        if (!technician.getAssignedRegion().equals(request.getRegion())) {
            result.addViolation("Technician region does not match request region.");
        }

        if (startTime.isBefore(technician.getAvailabilityStart()) || endTime.isAfter(technician.getAvailabilityEnd())) {
            result.addViolation("Assignment is outside technician's available hours.");
        }

        if (request.getPreferredStartTime() != null && request.getPreferredEndTime() != null) {
            if (startTime.isBefore(request.getPreferredStartTime()) || endTime.isAfter(request.getPreferredEndTime())) {
                result.addViolation("Assignment does not fit within preferred time window.");
            }
        }

        // Calculate workload
        long currentWorkloadMinutes = existingAssignments.stream()
            .mapToLong(a -> java.time.Duration.between(a.getStartTime(), a.getEndTime()).toMinutes())
            .sum();
        long newAssignmentMinutes = java.time.Duration.between(startTime, endTime).toMinutes();

        if (currentWorkloadMinutes + newAssignmentMinutes > technician.getMaximumWorkloadMinutes()) {
            result.addViolation("Technician maximum workload exceeded.");
        }

        // Check overlaps
        for (Assignment existing : existingAssignments) {
            if (startTime.isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(endTime)) {
                result.addViolation("Requested time overlaps with an existing assignment.");
                break;
            }
        }

        return result;
    }
}
