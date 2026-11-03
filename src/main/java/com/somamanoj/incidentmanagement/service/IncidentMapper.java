package com.somamanoj.incidentmanagement.service;

import com.somamanoj.incidentmanagement.dto.CommentResponse;
import com.somamanoj.incidentmanagement.dto.HistoryResponse;
import com.somamanoj.incidentmanagement.dto.IncidentResponse;
import com.somamanoj.incidentmanagement.dto.UserSummaryResponse;
import com.somamanoj.incidentmanagement.entity.Incident;
import com.somamanoj.incidentmanagement.entity.IncidentComment;
import com.somamanoj.incidentmanagement.entity.IncidentHistory;
import com.somamanoj.incidentmanagement.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident, List<IncidentComment> comments) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory())
                .priority(incident.getPriority())
                .status(incident.getStatus())
                .reporter(toUser(incident.getReporter()))
                .assignee(toUser(incident.getAssignee()))
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolvedAt(incident.getResolvedAt())
                .comments(comments.stream().map(this::toComment).toList())
                .build();
    }

    public CommentResponse toComment(IncidentComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .authorName(comment.getAuthor().getFullName())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public HistoryResponse toHistory(IncidentHistory history) {
        return HistoryResponse.builder()
                .id(history.getId())
                .action(history.getAction())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .details(history.getDetails())
                .changedBy(history.getChangedBy() != null ? history.getChangedBy().getFullName() : "System")
                .createdAt(history.getCreatedAt())
                .build();
    }

    public UserSummaryResponse toUser(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
