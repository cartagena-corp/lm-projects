package com.cartagenacorp.lm_projects.service;

import com.cartagenacorp.lm_projects.dto.PageResponseDTO;
import com.cartagenacorp.lm_projects.dto.ProjectDTO;
import com.cartagenacorp.lm_projects.entity.Project;
import com.cartagenacorp.lm_projects.mapper.ProjectMapper;
import com.cartagenacorp.lm_projects.repository.ProjectRepository;
import com.cartagenacorp.lm_projects.util.JwtContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserValidationService userValidationService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserValidationService userValidationService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userValidationService = userValidationService;
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectDTO> getAllProjects(String name, String status, Pageable pageable){
        Page<Project> projectPage;
        if (name != null && !name.isEmpty() && status != null && !status.isEmpty()) {
            projectPage = projectRepository.findByNameContainingIgnoreCaseAndStatus(name, status, pageable);
        } else if (name != null && !name.isEmpty()) {
            projectPage = projectRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (status != null && !status.isEmpty()) {
            projectPage = projectRepository.findByStatus(status, pageable);
        } else {
            projectPage = projectRepository.findAll(pageable);
        }
        return new PageResponseDTO<>(projectPage.map(project -> projectMapper.projectToProjectDTO(project)));
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(UUID id){
        return projectMapper.projectToProjectDTO(projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")));
    }

    @Transactional
    public void deleteProject(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO){
        if(projectDTO == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        UUID userId = JwtContextHolder.getUserId();
        projectDTO.setCreatedBy(userId);

        Project project = projectMapper.projectDTOToProject(projectDTO);
        projectRepository.save(project);
        return projectMapper.projectToProjectDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(ProjectDTO projectDTO, UUID id){
        if(projectDTO == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setStatus(projectDTO.getStatus());

        projectRepository.save(project) ;

        return projectMapper.projectToProjectDTO(project);
    }

    @Transactional(readOnly = true)
    public boolean projectExists(UUID id){
        return projectRepository.existsById(id);
    }
}
