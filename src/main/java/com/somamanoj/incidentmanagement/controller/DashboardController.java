package com.somamanoj.incidentmanagement.controller;

import com.somamanoj.incidentmanagement.dto.DashboardStatsResponse;
import com.somamanoj.incidentmanagement.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IncidentService incidentService;

    public DashboardController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> stats() {
        return ResponseEntity.ok(incidentService.dashboard());
    }
}
