package com.somamanoj.incidentmanagement.dto;

import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be 150 characters or less")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be 2000 characters or less")
    private String description;

    @NotNull(message = "Category is required")
    private IssueCategory category;

    @NotNull(message = "Priority is required")
    private Priority priority;
}
