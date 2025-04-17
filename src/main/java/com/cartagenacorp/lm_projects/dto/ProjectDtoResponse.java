package com.cartagenacorp.lm_projects.dto;

import com.cartagenacorp.lm_projects.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Project}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDtoResponse implements Serializable {
    UUID id;
    String name;
    String description;
    LocalDate startDate;
    LocalDate endDate;
    Long status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    CreatedByDto createdBy;
}