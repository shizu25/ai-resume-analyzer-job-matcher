package com.resumematcher.simulator.repository;

import com.resumematcher.simulator.entity.ResumeImprovementSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeImprovementSuggestionRepository extends JpaRepository<ResumeImprovementSuggestion, UUID> {
    List<ResumeImprovementSuggestion> findAllByVersionIdOrderByCreatedAtDesc(UUID versionId);
    List<ResumeImprovementSuggestion> findAllByVersionIdAndJobIdOrderByCreatedAtDesc(UUID versionId, UUID jobId);
}
