package com.resumematcher.roadmap.repository;

import com.resumematcher.roadmap.entity.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, UUID> {
    List<RoadmapItem> findAllByRoadmapId(UUID roadmapId);
}
