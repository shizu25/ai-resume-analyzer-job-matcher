package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.ResumeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProject, UUID> {
    List<ResumeProject> findAllByResumeId(UUID resumeId);
    void deleteByResumeId(UUID resumeId);
}
