package com.cartagenacorp.lm_projects.dto;

import com.cartagenacorp.lm_projects.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link Project}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDtoRequest implements Serializable {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @NotNull(message = "Status is required")
    private Long status;
}