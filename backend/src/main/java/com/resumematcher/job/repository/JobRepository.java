package com.resumematcher.job.repository;

import com.resumematcher.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByIdAndIsActiveTrue(UUID id);
    List<Job> findAllByIsActiveTrueOrderByCreatedAtDesc();
}
