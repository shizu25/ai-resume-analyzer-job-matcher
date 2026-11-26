package com.resumematcher.simulator.repository;

import com.resumematcher.simulator.entity.ResumeSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSimulationRepository extends JpaRepository<ResumeSimulation, UUID> {
    List<ResumeSimulation> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<ResumeSimulation> findAllByBaseVersionIdOrImprovedVersionIdOrderByCreatedAtDesc(UUID baseId, UUID improvedId);
}
