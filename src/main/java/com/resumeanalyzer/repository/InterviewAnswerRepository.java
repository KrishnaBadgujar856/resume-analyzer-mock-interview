package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findBySessionId(Long sessionId);

    Optional<InterviewAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    Optional<InterviewAnswer> findByIdAndUserId(Long id, Long userId);
}
