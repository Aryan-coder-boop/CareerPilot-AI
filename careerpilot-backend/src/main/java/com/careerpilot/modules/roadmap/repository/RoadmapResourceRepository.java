package com.careerpilot.modules.roadmap.repository;

import com.careerpilot.modules.roadmap.entity.RoadmapResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapResourceRepository extends JpaRepository<RoadmapResource, UUID> {
    List<RoadmapResource> findByMilestoneId(UUID milestoneId);
}

