package com.careerpilot.modules.roadmap.repository;

import com.careerpilot.modules.roadmap.entity.RoadmapMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapMilestoneRepository extends JpaRepository<RoadmapMilestone, UUID> {
    List<RoadmapMilestone> findByRoadmapIdOrderByWeekNumberAscOrderIndexAsc(UUID roadmapId);
}

