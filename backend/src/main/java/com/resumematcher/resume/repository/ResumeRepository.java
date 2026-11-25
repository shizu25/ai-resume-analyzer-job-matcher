package com.resumematcher.resume.repository;

import com.resumematcher.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Resume> findByIdAndUserId(UUID id, UUID userId);
    Optional<Resume> findByUserIdAndIsActiveTrue(UUID userId);
}
