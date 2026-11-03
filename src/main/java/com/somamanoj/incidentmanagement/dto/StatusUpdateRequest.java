package com.somamanoj.incidentmanagement.dto;

import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private IncidentStatus status;

    @Size(max = 2000, message = "Comment must be 2000 characters or less")
    private String comment;
}
