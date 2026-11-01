package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.InterviewSession;
import com.resumeanalyzer.entity.InterviewSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, InterviewSessionStatus status);

    @Query("SELECT AVG(s.overallScore) FROM InterviewSession s WHERE s.user.id = :userId AND s.overallScore IS NOT NULL")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId AND " +
           "(:role IS NULL OR LOWER(s.targetRole) LIKE LOWER(CONCAT('%', :role, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.createdAt DESC")
    List<InterviewSession> findByUserIdWithFilters(@Param("userId") Long userId,
                                                  @Param("role") String role,
                                                  @Param("status") InterviewSessionStatus status);
}
