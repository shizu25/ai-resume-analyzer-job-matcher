package com.resumematcher.job.repository;

import com.resumematcher.job.entity.ExperienceLevel;
import com.resumematcher.job.entity.Job;
import com.resumematcher.job.entity.JobType;
import com.resumematcher.job.entity.WorkMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class JobSpecification {

    private JobSpecification() {}

    public static Specification<Job> withFilters(String query,
                                                String skill,
                                                String location,
                                                WorkMode workMode,
                                                JobType jobType,
                                                ExperienceLevel experienceLevel) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("company")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (skill != null && !skill.isBlank()) {
                String[] tokens = skill.split(",");
                for (String token : tokens) {
                    String trimmed = token.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        predicates.add(cb.like(cb.lower(root.get("requiredSkillsRaw")), "%" + trimmed + "%"));
                    }
                }
            }

            if (location != null && !location.isBlank()) {
                String locPattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            if (workMode != null) {
                predicates.add(cb.equal(root.get("workMode"), workMode));
            }

            if (jobType != null) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }

            if (experienceLevel != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
