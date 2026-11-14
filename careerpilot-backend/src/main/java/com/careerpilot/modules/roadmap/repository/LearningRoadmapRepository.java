package com.careerpilot.modules.roadmap.repository;

import com.careerpilot.modules.roadmap.entity.LearningRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, UUID> {
    List<LearningRoadmap> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<LearningRoadmap> findByIdAndUserId(UUID id, UUID userId);
}

