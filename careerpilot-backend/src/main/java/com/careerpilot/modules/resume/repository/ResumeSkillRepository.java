package com.careerpilot.modules.resume.repository;

import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, UUID> {

    List<ResumeSkill> findByResumeId(UUID resumeId);

    void deleteByResume(Resume resume);
}

