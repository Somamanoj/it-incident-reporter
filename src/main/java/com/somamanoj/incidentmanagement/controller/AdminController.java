package com.somamanoj.incidentmanagement.controller;

import com.somamanoj.incidentmanagement.dto.UserSummaryResponse;
import com.somamanoj.incidentmanagement.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final IncidentService incidentService;

    public AdminController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserSummaryResponse>> users() {
        return ResponseEntity.ok(incidentService.allUsers());
    }

    @GetMapping("/engineers")
    public ResponseEntity<List<UserSummaryResponse>> engineers() {
        return ResponseEntity.ok(incidentService.supportEngineers());
    }
}
