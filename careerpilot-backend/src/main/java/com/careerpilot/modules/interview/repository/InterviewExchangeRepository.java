package com.careerpilot.modules.interview.repository;

import com.careerpilot.modules.interview.entity.InterviewExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewExchangeRepository extends JpaRepository<InterviewExchange, UUID> {
    List<InterviewExchange> findBySessionIdOrderByQuestionIndexAsc(UUID sessionId);
    Optional<InterviewExchange> findBySessionIdAndQuestionIndex(UUID sessionId, int questionIndex);
}

