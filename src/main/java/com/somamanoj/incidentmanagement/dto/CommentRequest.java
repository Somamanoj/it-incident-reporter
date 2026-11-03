package com.somamanoj.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must be 2000 characters or less")
    private String comment;
}
