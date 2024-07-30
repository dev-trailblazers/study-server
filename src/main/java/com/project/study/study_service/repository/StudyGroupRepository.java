package com.project.study.study_service.repository;

import com.project.study.study_service.domain.studygroup.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
}
