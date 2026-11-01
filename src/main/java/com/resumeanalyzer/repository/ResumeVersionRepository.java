package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByResumeIdOrderByVersionNumberDesc(Long resumeId);

    Optional<ResumeVersion> findTopByResumeIdOrderByVersionNumberDesc(Long resumeId);
}
