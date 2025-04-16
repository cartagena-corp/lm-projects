package com.cartagenacorp.lm_projects.mapper;

import com.cartagenacorp.lm_projects.entity.Project;
import com.cartagenacorp.lm_projects.dto.ProjectDtoRequest;
import com.cartagenacorp.lm_projects.dto.ProjectDtoResponse;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    Project toEntity(ProjectDtoRequest projectDtoRequest);
    ProjectDtoResponse toDto(Project project);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Project partialUpdate(ProjectDtoRequest projectDtoRequest, @MappingTarget Project project);
}
