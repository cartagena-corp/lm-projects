package com.cartagenacorp.lm_projects.controller;

import com.cartagenacorp.lm_projects.dto.*;
import com.cartagenacorp.lm_projects.service.ProjectService;
import com.cartagenacorp.lm_projects.util.RequiresPermission;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @RequiresPermission({"PROJECT_CRUD", "PROJECT_READ"})
    public ResponseEntity<PageResponseDTO<ProjectDtoResponse>> getAllProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        UUID uuid = null;
        if (createdBy != null && !createdBy.isEmpty()) {
            uuid = UUID.fromString(createdBy);
        }
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        PageResponseDTO<ProjectDtoResponse> projectsDTOResponse = projectService.getAllProjects(name, status, uuid, pageable);
        return ResponseEntity.ok(projectsDTOResponse);
    }

    @GetMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD", "PROJECT_READ"})
    public ResponseEntity<?> getProjectById(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        ProjectDtoResponse projectDtoResponse = projectService.getProjectById(uuid);
        return ResponseEntity.ok(projectDtoResponse);
    }

    @PostMapping
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> createProject(@RequestBody @Valid ProjectDtoRequest projectDtoRequest) {
        ProjectDtoResponse createdProject = projectService.createProject(projectDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PutMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody @Valid ProjectDtoRequest projectDtoRequest) {
        UUID uuid = UUID.fromString(id);
        ProjectDtoResponse updatedProject = projectService.updateProject(projectDtoRequest, uuid);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        projectService.deleteProject(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/participants")
    @RequiresPermission({"PROJECT_CRUD", "PROJECT_READ"})
    public ResponseEntity<List<CreatedByDto>> getParticipants(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectParticipants(projectId));
    }

    @PostMapping("/{projectId}/participants")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> addParticipants(@PathVariable UUID projectId, @RequestBody ProjectParticipantRequestDto request) {
        projectService.addParticipants(projectId, request.getUserIds());
        return ResponseEntity.status(HttpStatus.CREATED).body("Participants added successfully");
    }

    @DeleteMapping("/{projectId}/participants")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> removeParticipants(@PathVariable UUID projectId, @RequestBody ProjectParticipantRequestDto request) {
        projectService.removeParticipants(projectId, request.getUserIds());
        return ResponseEntity.ok("Participants removed successfully");
    }

    @GetMapping("/validate/{id}")
    public ResponseEntity<Boolean> projectExists(@PathVariable String id){
        UUID uuid = UUID.fromString(id);
        return ResponseEntity.status(HttpStatus.OK).body(projectService.projectExists(uuid));
    }
}
