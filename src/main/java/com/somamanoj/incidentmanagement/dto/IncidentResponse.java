package com.somamanoj.incidentmanagement.dto;

import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class IncidentResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final IssueCategory category;
    private final Priority priority;
    private final IncidentStatus status;
    private final UserSummaryResponse reporter;
    private final UserSummaryResponse assignee;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime resolvedAt;
    private final List<CommentResponse> comments;
}
