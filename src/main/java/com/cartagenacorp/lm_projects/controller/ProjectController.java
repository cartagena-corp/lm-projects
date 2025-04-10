package com.cartagenacorp.lm_projects.controller;

import com.cartagenacorp.lm_projects.dto.PageResponseDTO;
import com.cartagenacorp.lm_projects.dto.ProjectDTO;
import com.cartagenacorp.lm_projects.service.ProjectService;
import com.cartagenacorp.lm_projects.util.RequiresPermission;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<PageResponseDTO<ProjectDTO>> getAllProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageResponseDTO<ProjectDTO> projectsDTO = projectService.getAllProjects(name, status, pageable);
        return ResponseEntity.ok(projectsDTO);
    }

    @GetMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD", "PROJECT_READ"})
    public ResponseEntity<?> getProjectById(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            ProjectDTO projectDTO = projectService.getProjectById(uuid);
            return ResponseEntity.ok(projectDTO);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            ProjectDTO createdProject = projectService.createProject(projectDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> updateProject(@PathVariable String id, @RequestBody ProjectDTO projectDTO) {

        try {
            UUID uuid = UUID.fromString(id);
            ProjectDTO updatedProject = projectService.updateProject(projectDTO, uuid);
            return ResponseEntity.ok(updatedProject);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }  catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }  catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequiresPermission({"PROJECT_CRUD"})
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            projectService.deleteProject(uuid);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }  catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @GetMapping("/validate/{id}")
    public ResponseEntity<Boolean> projectExists(@PathVariable String id){
        try {
            UUID uuid = UUID.fromString(id);
            return ResponseEntity.status(HttpStatus.OK).body(projectService.projectExists(uuid));
        }  catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
