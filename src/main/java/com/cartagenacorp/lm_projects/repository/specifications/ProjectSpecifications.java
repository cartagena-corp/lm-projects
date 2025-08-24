package com.cartagenacorp.lm_projects.repository.specifications;

import com.cartagenacorp.lm_projects.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
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

    public static Specification<Project> isCreatorOrParticipant(UUID userId, List<UUID> participantProjectIds) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.disjunction();
            }

            Predicate createdBy = cb.equal(root.get("createdBy"), userId);

            if (participantProjectIds != null && !participantProjectIds.isEmpty()) {
                Predicate participant = root.get("id").in(participantProjectIds);
                return cb.or(createdBy, participant);
            }

            return createdBy;
        };
    }

    public static Specification<Project> hasOrganization(UUID organizationId) {
        return (root, query, cb) -> {
            if (organizationId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("organizationId"), organizationId);
        };
    }
}
