package com.cartagenacorp.lm_projects.repository;

import com.cartagenacorp.lm_projects.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsById(UUID id);

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByStatus(String status, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}
