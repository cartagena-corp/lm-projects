package com.cartagenacorp.lm_projects.service;

import com.cartagenacorp.lm_projects.dto.PageResponseDTO;
import com.cartagenacorp.lm_projects.dto.ProjectDtoRequest;
import com.cartagenacorp.lm_projects.dto.ProjectDtoResponse;
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

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectDtoResponse> getAllProjects(String name, String status, Pageable pageable){
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
        return new PageResponseDTO<>(projectPage.map(projectMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ProjectDtoResponse getProjectById(UUID id){
        return projectMapper.toDto(projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")));
    }

    @Transactional
    public void deleteProject(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDtoResponse createProject(ProjectDtoRequest projectDtoRequest){
        if(projectDtoRequest == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        UUID userId = JwtContextHolder.getUserId();

        Project project = projectMapper.toEntity(projectDtoRequest);
        project.setCreatedBy(userId);
        projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    @Transactional
    public ProjectDtoResponse updateProject(ProjectDtoRequest projectDtoRequest, UUID id){
        if(projectDtoRequest == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        project.setName(projectDtoRequest.getName());
        project.setDescription(projectDtoRequest.getDescription());
        project.setStartDate(projectDtoRequest.getStartDate());
        project.setEndDate(projectDtoRequest.getEndDate());
        project.setStatus(projectDtoRequest.getStatus());

        projectRepository.save(project) ;

        return projectMapper.toDto(project);
    }

    @Transactional(readOnly = true)
    public boolean projectExists(UUID id){
        return projectRepository.existsById(id);
    }
}
