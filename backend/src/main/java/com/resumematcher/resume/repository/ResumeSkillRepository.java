package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, UUID> {
    List<ResumeSkill> findAllByResumeId(UUID resumeId);
    void deleteByResumeId(UUID resumeId);
}
