package com.cartagenacorp.lm_projects.config;

import com.cartagenacorp.lm_projects.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ProjectMapper projectMapper() {
        return Mappers.getMapper(ProjectMapper.class);
    }
}
