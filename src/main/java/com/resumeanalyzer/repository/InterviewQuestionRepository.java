package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findBySessionIdOrderByQuestionOrderAsc(Long sessionId);

    Optional<InterviewQuestion> findByIdAndSessionId(Long id, Long sessionId);
}
