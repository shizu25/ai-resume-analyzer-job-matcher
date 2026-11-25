package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, UUID> {
    List<ResumeExperience> findAllByResumeId(UUID resumeId);
    void deleteByResumeId(UUID resumeId);
}
