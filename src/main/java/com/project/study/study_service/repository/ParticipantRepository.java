package com.project.study.study_service.repository;

import com.project.study.study_service.domain.participants.Participants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participants, Long> {

    List<Participants> findByMember_id(Long memberId);

    List<Participants> findByStudyGroup_id(Long studyId);
}
