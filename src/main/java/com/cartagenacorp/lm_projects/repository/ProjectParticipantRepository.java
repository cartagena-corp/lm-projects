package com.cartagenacorp.lm_projects.repository;

import com.cartagenacorp.lm_projects.entity.ProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, UUID> {
    List<ProjectParticipant> findByUserId(UUID userId);
    List<ProjectParticipant> findByProjectId(UUID projectId);
    List<ProjectParticipant> findByProjectIdAndUserIdIn(UUID projectId, List<UUID> userIds);
}