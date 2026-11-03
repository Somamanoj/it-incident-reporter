package com.somamanoj.incidentmanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private final Long id;
    private final String comment;
    private final String authorName;
    private final LocalDateTime createdAt;
}
