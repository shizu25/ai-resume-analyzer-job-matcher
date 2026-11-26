package com.resumematcher.simulator.repository;

import com.resumematcher.simulator.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
    List<ResumeVersion> findAllByParentResumeIdOrderByVersionNumberAsc(UUID parentResumeId);
    List<ResumeVersion> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<ResumeVersion> findByParentResumeIdAndVersionNumber(UUID parentResumeId, Integer versionNumber);

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM ResumeVersion v WHERE v.parentResume.id = :parentResumeId")
    Integer findMaxVersionNumberByParentResumeId(UUID parentResumeId);
}
