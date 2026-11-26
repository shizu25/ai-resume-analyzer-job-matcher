package com.resumematcher.matching.repository;

import com.resumematcher.matching.entity.JobMatch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT m FROM JobMatch m
            JOIN FETCH m.job j
            WHERE m.user.id = :userId
              AND m.resume.id = :resumeId
              AND j.isActive = true
            ORDER BY m.matchScore DESC, m.updatedAt DESC, j.title ASC
            """)
    List<JobMatch> findTopActiveMatchesByUserAndResume(
            @Param("userId") UUID userId,
            @Param("resumeId") UUID resumeId,
            Pageable pageable
    );

    @Query("""
            SELECT m FROM JobMatch m
            JOIN FETCH m.job j
            WHERE m.user.id = :userId
              AND j.isActive = true
              AND NOT EXISTS (
                  SELECT 1 FROM JobMatch m2
                  WHERE m2.user.id = :userId
                    AND m2.job.id = m.job.id
                    AND (
                        m2.matchScore > m.matchScore
                        OR (m2.matchScore = m.matchScore AND m2.updatedAt > m.updatedAt)
                        OR (m2.matchScore = m.matchScore AND m2.updatedAt = m.updatedAt AND m2.id > m.id)
                    )
              )
            ORDER BY m.matchScore DESC, m.updatedAt DESC, j.title ASC
            """)
    List<JobMatch> findTopActiveMatchesByUser(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
