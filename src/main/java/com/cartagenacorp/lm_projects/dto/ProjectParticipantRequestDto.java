package com.cartagenacorp.lm_projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link com.cartagenacorp.lm_projects.entity.ProjectParticipant}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectParticipantRequestDto implements Serializable {
    private List<UUID> userIds;
}