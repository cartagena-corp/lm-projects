package com.cartagenacorp.lm_projects.service;

import com.cartagenacorp.lm_projects.dto.*;
import com.cartagenacorp.lm_projects.entity.Project;
import com.cartagenacorp.lm_projects.entity.ProjectParticipant;
import com.cartagenacorp.lm_projects.mapper.ProjectMapper;
import com.cartagenacorp.lm_projects.repository.ProjectParticipantRepository;
import com.cartagenacorp.lm_projects.repository.ProjectRepository;
import com.cartagenacorp.lm_projects.repository.specifications.ProjectSpecifications;
import com.cartagenacorp.lm_projects.util.JwtContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserExternalService userExternalService;
    private final ConfigExternalService configExternalService;
    private final ProjectParticipantRepository projectParticipantRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserExternalService userExternalService,
                          ConfigExternalService configExternalService, ProjectParticipantRepository projectParticipantRepository) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userExternalService = userExternalService;
        this.configExternalService = configExternalService;
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

        List<UserBasicDataDto> createdByList = userExternalService.getUsersData(
                JwtContextHolder.getToken(),
                createdByIds.stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList())
        );

        Map<UUID, UserBasicDataDto> createdByMap = createdByList.stream()
                .collect(Collectors.toMap(UserBasicDataDto::getId, Function.identity()));

        for (int i = 0; i < dtoList.size(); i++) {
            UUID createdById = projectPage.getContent().get(i).getCreatedBy();
            UserBasicDataDto createdByDto = createdByMap.getOrDefault(
                    createdById,
                    new UserBasicDataDto(createdById, null, null, null, null, null, null)
            );
            dtoList.get(i).setCreatedBy(createdByDto);
        }
        return new PageResponseDTO<>(new PageImpl<>(dtoList, pageable, projectPage.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ProjectDtoResponse getProjectById(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        UUID userId = JwtContextHolder.getUserId();
        boolean isCreator = project.getCreatedBy().equals(userId);
        boolean isParticipant = projectParticipantRepository.existsByProjectIdAndUserId(id, userId);

        if (!isCreator && !isParticipant) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        ProjectDtoResponse projectDtoResponse = projectMapper.toDto(project);

        List<UserBasicDataDto> createdByList = userExternalService.getUsersData(
                JwtContextHolder.getToken(),
                List.of(project.getCreatedBy().toString())
        );

        UserBasicDataDto createdByDto = createdByList.isEmpty()
                ? new UserBasicDataDto(project.getCreatedBy(), null, null, null, null, null, null)
                : createdByList.get(0);

        return new ProjectDtoResponse(
                projectDtoResponse.getId(),
                projectDtoResponse.getName(),
                projectDtoResponse.getDescription(),
                projectDtoResponse.getStartDate(),
                projectDtoResponse.getEndDate(),
                projectDtoResponse.getStatus(),
                projectDtoResponse.getCreatedAt(),
                projectDtoResponse.getUpdatedAt(),
                createdByDto,
                projectDtoResponse.getOrganizationId()
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
        configExternalService.deleteByProjectId(id, JwtContextHolder.getToken());
        projectParticipantRepository.deleteByProjectId(id);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDtoResponse createProject(ProjectDtoRequest projectDtoRequest){
        if(projectDtoRequest == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        UUID userId = JwtContextHolder.getUserId();
        UUID organizationId = JwtContextHolder.getOrganizationId();

        Project project = projectMapper.toEntity(projectDtoRequest);
        project.setCreatedBy(userId);
        project.setOrganizationId(organizationId);
        Project savedProject = projectRepository.save(project);

        if (savedProject.getId() != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    configExternalService.initializeDefaultProjectConfig(savedProject.getId(), JwtContextHolder.getToken());
                }
            });
        }

        return projectMapper.toDto(project);
    }

    @Transactional
    public ProjectDtoResponse updateProject(ProjectDtoRequest projectDtoRequest, UUID id){
        if(projectDtoRequest == null){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project cannot be null"); }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        UUID userId = JwtContextHolder.getUserId();
        boolean isCreator = project.getCreatedBy().equals(userId);
        boolean isParticipant = projectParticipantRepository.existsByProjectIdAndUserId(id, userId);

        if (!isCreator && !isParticipant) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        project.setName(projectDtoRequest.getName());
        project.setDescription(projectDtoRequest.getDescription());
        project.setStartDate(projectDtoRequest.getStartDate());
        project.setEndDate(projectDtoRequest.getEndDate());
        project.setStatus(projectDtoRequest.getStatus());

        projectRepository.save(project) ;

        return projectMapper.toDto(project);
    }

    @Transactional(readOnly = true)
    public List<UserBasicDataDto> getProjectParticipants(UUID projectId) {
        List<UUID> userIds = projectParticipantRepository.findByProjectId(projectId)
                .stream()
                .map(ProjectParticipant::getUserId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            return List.of();
        }

        UUID creatorId = optionalProject.get().getCreatedBy();
        if (!userIds.contains(creatorId)) {
            userIds.add(creatorId);
        }

        if (userIds.isEmpty()) {
            return List.of();
        }

        List<UserBasicDataDto> users = userExternalService.getUsersData(
                JwtContextHolder.getToken(),
                userIds.stream()
                        .map(UUID::toString)
                        .toList()
        );

        return users.stream()
                .sorted(Comparator.comparing(UserBasicDataDto::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();
    }

    @Transactional
    public void addParticipants(UUID projectId, List<UUID> userIds) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        UUID currentUserId = JwtContextHolder.getUserId();
        boolean isCreator = project.getCreatedBy().equals(currentUserId);
        boolean isParticipant = projectParticipantRepository.existsByProjectIdAndUserId(projectId, currentUserId);

        if (!isCreator && !isParticipant) {
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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        UUID userId = JwtContextHolder.getUserId();
        boolean isCreator = project.getCreatedBy().equals(userId);
        boolean isParticipant = projectParticipantRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isCreator && !isParticipant) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        List<ProjectParticipant> existing = projectParticipantRepository.findByProjectIdAndUserIdIn(projectId, userIds);
        projectParticipantRepository.deleteAll(existing);
    }

    @Transactional(readOnly = true)
    public boolean projectExists(UUID id){
        return projectRepository.existsById(id);
    }
}
