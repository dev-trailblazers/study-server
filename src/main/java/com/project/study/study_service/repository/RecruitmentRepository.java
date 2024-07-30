package com.project.study.study_service.repository;

import com.project.study.study_service.domain.recruitment.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {


    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE LOWER(sg.goal) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Recruitment> findAllRecruitmentsWithSearch(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE r.startDate > CURRENT_DATE " +
            "AND LOWER(sg.goal) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Recruitment> findAllRecruitmentsByScheduledWithSearch(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE CURRENT_DATE BETWEEN r.startDate AND r.endDate " +
            "AND LOWER(sg.goal) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Recruitment> findAllRecruitmentsByInProgressWithSearch(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.studyGroup sg " +
            "WHERE r.endDate < CURRENT_DATE " +
            "AND LOWER(sg.goal) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Recruitment> findAllRecruitmentsByClosedWithSearch(@Param("keyword") String keyword, Pageable pageable);


}
