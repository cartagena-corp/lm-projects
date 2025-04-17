package com.cartagenacorp.lm_projects.repository.specifications;

import com.cartagenacorp.lm_projects.entity.Project;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProjectSpecifications {

    public static Specification<Project> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Project> hasStatus(Long status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Project> hasCreatedBy(UUID createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (createdBy == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }
}
