package com.cartagenacorp.lm_projects.mapper;

import com.cartagenacorp.lm_projects.dto.ProjectDTO;
import com.cartagenacorp.lm_projects.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project projectDTOToProject(ProjectDTO projectDTO);

    ProjectDTO projectToProjectDTO(Project project);

    List<ProjectDTO> projectsToProjectsDTOs(List<Project> projects);

    List<Project> projectsDTOsToProjects(List<ProjectDTO> projectDTOs);

}
