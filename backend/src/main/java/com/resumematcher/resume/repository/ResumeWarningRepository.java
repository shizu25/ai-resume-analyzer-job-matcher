package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.ResumeWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeWarningRepository extends JpaRepository<ResumeWarning, UUID> {
    List<ResumeWarning> findAllByResumeId(UUID resumeId);
    void deleteByResumeId(UUID resumeId);
}
