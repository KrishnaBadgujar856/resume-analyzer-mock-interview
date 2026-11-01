package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT DISTINCT r FROM Resume r LEFT JOIN r.skills s WHERE r.user.id = :userId AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.originalFilename) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(r.targetRole, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(r.extractedText, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<Resume> searchResumes(@Param("userId") Long userId, @Param("query") String query);
}
