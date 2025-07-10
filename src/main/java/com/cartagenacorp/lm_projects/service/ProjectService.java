package com.cartagenacorp.lm_projects.service;

import com.cartagenacorp.lm_projects.dto.*;
import com.cartagenacorp.lm_projects.entity.Project;
import com.cartagenacorp.lm_projects.entity.ProjectParticipant;
import com.cartagenacorp.lm_projects.mapper.ProjectMapper;
import com.cartagenacorp.lm_projects.repository.ProjectParticipantRepository;
import com.cartagenacorp.lm_projects.repository.ProjectRepository;
import com.cartagenacorp.lm_projects.repository.specifications.ProjectSpecifications;
import com.cartagenacorp.lm_projects.util.JwtContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserValidationService userValidationService;
    private final ProjectParticipantRepository projectParticipantRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserValidationService userValidationService,
                          ProjectParticipantRepository projectParticipantRepository) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userValidationService = userValidationService;
        this.projectParticipantRepository = projectParticipantRepository;
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectDtoResponse> getAllProjects(String name, Long status, UUID createdBy, Pageable pageable) {
        UUID userId = JwtContextHolder.getUserId();

        Specification<Project> spec = Specification.where(ProjectSpecifications.hasName(name))
                .and(ProjectSpecifications.hasStatus(status));

        if (createdBy != null) {
            spec = spec.and(ProjectSpecifications.hasCreatedBy(createdBy));
        } else {
            List<UUID> participantProjectIds = projectParticipantRepository.findByUserId(userId)
                    .stream()
                    .map(ProjectParticipant::getProjectId)
                    .toList();

            spec = spec.and(ProjectSpecifications.isCreatorOrParticipant(userId, participantProjectIds));
        }

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);

        List<ProjectDtoResponse> dtoList = projectPage
                .stream()
                .map(projectMapper::toDto)
                .toList();

        List<UUID> createdByIds = projectPage.stream()
                .map(Project::getCreatedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Optional<List<CreatedByDto>> createdByListOpt = userValidationService.getUsersData(
                JwtContextHolder.getToken(), createdByIds.stream().map(UUID::toString).collect(Collectors.toList())
        );

        Map<UUID, CreatedByDto> createdByMap = createdByListOpt
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(CreatedByDto::getId, Function.identity()));

        for (int i = 0; i < dtoList.size(); i++) {
            UUID createdById = projectPage.getContent().get(i).getCreatedBy();
            CreatedByDto createdByDto = createdByMap.getOrDefault(
                    createdById,
                    new CreatedByDto(createdById, null, null, null, null, null)
            );
            dtoList.get(i).setCreatedBy(createdByDto);
        }
        return new PageResponseDTO<>(new PageImpl<>(dtoList, pageable, projectPage.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ProjectDtoResponse getProjectById(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        ProjectDtoResponse dto = projectMapper.toDto(project);
        Optional<CreatedByDto> createdBy = userValidationService.getUserData(JwtContextHolder.getToken(), project.getCreatedBy().toString());

        return new ProjectDtoResponse(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getStatus(),
                dto.getCreatedAt(),
                dto.getUpdatedAt(),
                new CreatedByDto(
                        createdBy.map(CreatedByDto::getId).orElse(project.getCreatedBy()),
                        createdBy.map(CreatedByDto::getFirstName).orElse(null),
                        createdBy.map(CreatedByDto::getLastName).orElse(null),
                        createdBy.map(CreatedByDto::getPicture).orElse(null),
                        createdBy.map(CreatedByDto::getEmail).orElse(null),
                        createdBy.map(CreatedByDto::getRole).orElse(null)
                )
        );
    }

    @Transactional
    public void deleteProject(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        UUID userId = JwtContextHolder.getUserId();

        if (!project.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this project");
        }

        projectParticipantRepository.deleteByProjectId(id);
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
    public List<CreatedByDto> getProjectParticipants(UUID projectId) {
        List<UUID> userIds = projectParticipantRepository.findByProjectId(projectId)
                .stream()
                .map(ProjectParticipant::getUserId)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return List.of();
        }
        Optional<List<CreatedByDto>> users = userValidationService.getUsersData(
                JwtContextHolder.getToken(), // Token del usuario autenticado
                userIds.stream().map(UUID::toString).toList()
        );

        return users.orElse(List.of());
    }

    @Transactional
    public void addParticipants(UUID projectId, List<UUID> userIds) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        List<UUID> existingUserIds = projectParticipantRepository.findByProjectId(projectId)
                .stream()
                .map(ProjectParticipant::getUserId)
                .toList();

        List<ProjectParticipant> newParticipants = userIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .map(userId -> new ProjectParticipant(null, projectId, userId))
                .toList();

        projectParticipantRepository.saveAll(newParticipants);
    }

    @Transactional
    public void removeParticipants(UUID projectId, List<UUID> userIds) {
        List<ProjectParticipant> existing = projectParticipantRepository.findByProjectIdAndUserIdIn(projectId, userIds);
        projectParticipantRepository.deleteAll(existing);
    }

    @Transactional(readOnly = true)
    public boolean projectExists(UUID id){
        return projectRepository.existsById(id);
    }
}
