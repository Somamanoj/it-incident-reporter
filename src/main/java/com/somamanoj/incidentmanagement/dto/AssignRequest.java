package com.somamanoj.incidentmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRequest {

    @NotNull(message = "Engineer id is required")
    private Long engineerId;
}
