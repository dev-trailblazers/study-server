package com.project.study.study_service.repository;

import com.project.study.study_service.domain.recruitment.Recruitment;
import com.project.study.study_service.domain.recruitment.RecruitmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    Optional<Recruitment> findByCreatedByAndStudyGroupId(Long memberId, Long studyGroupId);

    @Query("SELECT DISTINCT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE (:searchType = 'GOAL' AND sg.goal LIKE CONCAT('%', :keyword, '%')) " +
            "OR (:searchType = 'NAME' AND sg.name LIKE CONCAT('%', :keyword, '%'))")
    Page<Recruitment> findAllRecruitmentsWithSearch(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            Pageable pageable
    );

    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE (:status = 'SCHEDULED' AND r.startDate > CURRENT_DATE) " +
            "   OR (:status = 'IN_PROGRESS' AND CURRENT_DATE BETWEEN r.startDate AND r.endDate) " +
            "   OR (:status = 'CLOSED' AND r.endDate < CURRENT_DATE) " +
            "AND ((:searchType = 'GOAL' AND sg.goal LIKE CONCAT('%', :keyword, '%')) " +
            "     OR (:searchType = 'NAME' AND sg.name LIKE CONCAT('%', :keyword, '%')))")
    Page<Recruitment> findRecruitmentsByStatusAndSearch(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("status") String status,
            Pageable pageable
    );

}
