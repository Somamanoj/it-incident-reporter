package com.somamanoj.incidentmanagement.controller;

import com.somamanoj.incidentmanagement.dto.AssignRequest;
import com.somamanoj.incidentmanagement.dto.CommentRequest;
import com.somamanoj.incidentmanagement.dto.CommentResponse;
import com.somamanoj.incidentmanagement.dto.HistoryResponse;
import com.somamanoj.incidentmanagement.dto.IncidentRequest;
import com.somamanoj.incidentmanagement.dto.IncidentResponse;
import com.somamanoj.incidentmanagement.dto.StatusUpdateRequest;
import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import com.somamanoj.incidentmanagement.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> list(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IssueCategory category
    ) {
        return ResponseEntity.ok(incidentService.search(id, priority, status, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(incidentService.updateStatus(id, request));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<IncidentResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request
    ) {
        return ResponseEntity.ok(incidentService.assign(id, request));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> comment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addComment(id, request));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HistoryResponse>> history(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.history(id));
    }
}
