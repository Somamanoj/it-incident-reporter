package com.somamanoj.incidentmanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HistoryResponse {
    private final Long id;
    private final String action;
    private final String oldValue;
    private final String newValue;
    private final String details;
    private final String changedBy;
    private final LocalDateTime createdAt;
}
