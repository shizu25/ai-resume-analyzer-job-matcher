package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.ResumeSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSuggestionRepository extends JpaRepository<ResumeSuggestion, UUID> {
    List<ResumeSuggestion> findAllByResumeId(UUID resumeId);
    void deleteByResumeId(UUID resumeId);
}
