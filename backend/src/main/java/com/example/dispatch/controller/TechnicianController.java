package com.example.dispatch.controller;

import com.example.dispatch.constants.ApiEndpoints;
import com.example.dispatch.entity.Technician;
import com.example.dispatch.enums.TechnicianStatus;
import com.example.dispatch.repository.TechnicianRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.TECHNICIANS)

public class TechnicianController {

    private final TechnicianRepository technicianRepository;

    private final com.example.dispatch.service.ScheduleService scheduleService;

    public TechnicianController(TechnicianRepository technicianRepository, com.example.dispatch.service.ScheduleService scheduleService) {
        this.technicianRepository = technicianRepository;
        this.scheduleService = scheduleService;
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTechnician(@PathVariable Long id) {
        scheduleService.cancelTechnicianAndUnassign(id);
        return ResponseEntity.ok("Technician cancelled and requests returned to pending");
    }
}
