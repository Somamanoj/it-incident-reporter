package com.somamanoj.incidentmanagement.dto;

import com.somamanoj.incidentmanagement.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private final Long id;
    private final String fullName;
    private final String email;
    private final Role role;
}
