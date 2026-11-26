package com.resumematcher.matching.repository;

import com.resumematcher.matching.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, UUID> {
    Optional<JobMatch> findByResumeIdAndJobId(UUID resumeId, UUID jobId);
    Optional<JobMatch> findByIdAndUserId(UUID id, UUID userId);
    List<JobMatch> findAllByUserIdAndResumeIdOrderByMatchScoreDesc(UUID userId, UUID resumeId);
    List<JobMatch> findTop5ByUserIdAndResumeIdOrderByMatchScoreDesc(UUID userId, UUID resumeId);
}
