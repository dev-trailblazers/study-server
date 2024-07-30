package com.project.study.study_service.repository;

import com.project.study.study_service.domain.recruitment.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
}
