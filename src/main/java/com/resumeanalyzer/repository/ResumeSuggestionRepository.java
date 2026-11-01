package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.ResumeSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeSuggestionRepository extends JpaRepository<ResumeSuggestion, Long> {

    List<ResumeSuggestion> findByAnalysisId(Long analysisId);

    Optional<ResumeSuggestion> findByIdAndAnalysisResumeUserId(Long id, Long userId);
}
