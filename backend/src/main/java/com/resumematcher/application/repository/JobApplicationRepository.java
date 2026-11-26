package com.resumematcher.application.repository;

import com.resumematcher.application.entity.ApplicationStatus;
import com.resumematcher.application.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findAllByUserIdOrderByAppliedAtDesc(UUID userId);
    List<JobApplication> findAllByUserIdAndStatusOrderByAppliedAtDesc(UUID userId, ApplicationStatus status);
    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);
    Optional<JobApplication> findByUserIdAndJobId(UUID userId, UUID jobId);
    long countByUserId(UUID userId);
    long countByUserIdAndStatus(UUID userId, ApplicationStatus status);
}
