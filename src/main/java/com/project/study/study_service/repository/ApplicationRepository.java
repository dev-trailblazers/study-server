package com.project.study.study_service.repository;

import com.project.study.study_service.domain.applications.ApplicationStatus;
import com.project.study.study_service.domain.applications.Applications;
import com.project.study.study_service.domain.applications.ApplicationsDto;
import com.project.study.study_service.domain.applications.ApplicationsDto.ApplicationSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Applications, Long> {
    @Query("SELECT a FROM Applications a " +
            "JOIN FETCH a.studyGroup sg " +
            "WHERE a.member.id = :memberId " +
            "AND sg.id = :studyId " +
            "AND (a.status = 'WAITING' OR a.status = 'APPROVED')")
    Optional<Applications> findByMemberAndStudyAndStatus(@Param("memberId") Long memberId,
                                                         @Param("studyId") Long studyId);

    @Query("SELECT a " +
            "FROM Applications a " +
            "WHERE a.member.id = :#{#criteria.memberId} " +
            "AND a.studyGroup.name LIKE CONCAT('%', :#{#criteria.studyName}, '%')")
    Page<Applications> findAllByMemberIdAndStudyName(
            @Param("criteria") ApplicationSearchCriteria criteria,
            Pageable pageable);


    @Query("SELECT a " +
            "FROM Applications a " +
            "WHERE a.member.id = :#{#criteria.memberId} " +
            "AND a.studyGroup.name LIKE CONCAT('%', :#{#criteria.studyName}, '%') " +
            "AND a.status = :#{#criteria.status}")
    Page<Applications> findAllByMemberIdAndStudyNameAndStatus(
            @Param("criteria") ApplicationSearchCriteria criteria,
            Pageable pageable);
}