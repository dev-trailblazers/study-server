package com.project.study.study_service.repository;

import com.project.study.study_service.domain.studygroup.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByCreatedBy(Long memberId);
}
