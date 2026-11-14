package com.careerpilot.modules.jobmatch.repository;

import com.careerpilot.modules.jobmatch.entity.JobMatchReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobMatchReportRepository extends JpaRepository<JobMatchReport, UUID> {
    List<JobMatchReport> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<JobMatchReport> findByIdAndUserId(UUID id, UUID userId);
    List<JobMatchReport> findByResumeIdAndUserId(UUID resumeId, UUID userId);
}

