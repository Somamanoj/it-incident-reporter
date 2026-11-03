package com.somamanoj.incidentmanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DashboardStatsResponse {
    private final long totalIncidents;
    private final long openIncidents;
    private final long inProgressIncidents;
    private final long resolvedIncidents;
    private final long closedIncidents;
    private final long criticalIncidents;
    private final Map<String, Long> incidentsByCategory;
    private final Map<String, Long> incidentsByPriority;
}
