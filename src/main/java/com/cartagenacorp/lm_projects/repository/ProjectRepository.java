package com.cartagenacorp.lm_projects.repository;

import com.cartagenacorp.lm_projects.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    boolean existsById(UUID id);
}
