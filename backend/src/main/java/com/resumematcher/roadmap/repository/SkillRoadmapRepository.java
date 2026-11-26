package com.resumematcher.roadmap.repository;

import com.resumematcher.roadmap.entity.SkillRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRoadmapRepository extends JpaRepository<SkillRoadmap, UUID> {
    Optional<SkillRoadmap> findByUserIdAndJobIdAndResumeId(UUID userId, UUID jobId, UUID resumeId);
    List<SkillRoadmap> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
