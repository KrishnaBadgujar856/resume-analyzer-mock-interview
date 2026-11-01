package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    List<ResumeAnalysis> findByResumeIdOrderByCreatedAtDesc(Long resumeId);

    Optional<ResumeAnalysis> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);
}
