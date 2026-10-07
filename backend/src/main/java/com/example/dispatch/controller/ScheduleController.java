package com.example.dispatch.controller;

import com.example.dispatch.constants.ApiEndpoints;
import com.example.dispatch.constants.ErrorCode;
import com.example.dispatch.dto.request.ApprovalRequest;
import com.example.dispatch.entity.Assignment;
import com.example.dispatch.entity.Schedule;
import com.example.dispatch.entity.ScheduleVersion;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.entity.ServiceRequest;
import com.example.dispatch.enums.ScheduleStatus;
import com.example.dispatch.planner.PlannerService;
import com.example.dispatch.planner.ScheduleProposal;
import com.example.dispatch.repository.AssignmentRepository;
import com.example.dispatch.repository.ScheduleRepository;
import com.example.dispatch.repository.TechnicianRepository;
import com.example.dispatch.repository.ServiceRequestRepository;
import com.example.dispatch.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiEndpoints.SCHEDULES)

public class ScheduleController {

    private final PlannerService plannerService;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final AssignmentRepository assignmentRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceRequestRepository requestRepository;

    public ScheduleController(PlannerService plannerService, ScheduleService scheduleService,
                              ScheduleRepository scheduleRepository, AssignmentRepository assignmentRepository,
                              TechnicianRepository technicianRepository,
                              ServiceRequestRepository requestRepository) {
        this.plannerService = plannerService;
        this.scheduleService = scheduleService;
        this.scheduleRepository = scheduleRepository;
        this.assignmentRepository = assignmentRepository;
        this.technicianRepository = technicianRepository;
        this.requestRepository = requestRepository;
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/{date}")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        Schedule schedule = scheduleRepository.findByWorkingDate(parsedDate)
                .orElseGet(() -> {
                    Schedule newSchedule = new Schedule();
                    newSchedule.setWorkingDate(parsedDate);
                    newSchedule.setCurrentVersion(1L);
                    newSchedule.setStatus(ScheduleStatus.DRAFT);
                    return scheduleRepository.save(newSchedule);
                });

        List<Assignment> assignments = List.of();
        if (schedule.getCurrentVersion() != null) {
            assignments = assignmentRepository.findByScheduleVersionId(schedule.getCurrentVersion());
        }

        List<Technician> technicians = technicianRepository.findAll();
        List<ServiceRequest> requests = requestRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("schedule", schedule);
        response.put("assignments", assignments);
        response.put("technicians", technicians);
        response.put("requests", requests);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{date}/plan")
    public ResponseEntity<ScheduleProposal> generatePlan(@PathVariable String date) {
        ScheduleProposal proposal = plannerService.generatePlan(date);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping("/approve")
    public ResponseEntity<ScheduleVersion> approvePlan(@jakarta.validation.Valid @RequestBody ApprovalRequest request) {
        ScheduleVersion version = scheduleService.confirmScheduleProposal(
                request.getScheduleId(),
                request.getExpectedBaseVersion(),
                request.getProposal(),
                request.getActor(),
                request.getReason()
        );
        return ResponseEntity.ok(version);
    }
}
