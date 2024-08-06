package com.project.study.study_service.service;

import com.project.study.member_service.domain.member.MemberDto;
import com.project.study.study_service.domain.participants.Participants;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import com.project.study.study_service.repository.ParticipantRepository;
import com.project.study.study_service.repository.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final ParticipantRepository participantRepository;

    public StudyGroupDto registerStudyGroup(StudyGroupDto.Request dto){
        StudyGroup studyGroup = studyGroupRepository.save(dto.toEntity());
        return StudyGroupDto.fromEntity(studyGroup);
    }

    @Transactional(readOnly = true)
    public StudyGroupDto fetchStudyGroup(Long id){
        StudyGroup studyGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id + ": 해당하는 엔티티가 존재하지 않습니다."));
        return StudyGroupDto.fromEntity(studyGroup);
    }

    @Transactional(readOnly = true)
    public List<StudyGroupDto> fetchMyStudyGroups(Long memberId) {
        List<Participants> participants = participantRepository.findByMember_id(memberId);
        return participants.stream()
                .map(p -> StudyGroupDto.fromEntity(p.getStudyGroup()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberDto> fetchParticipantsByStudyId(Long studyId) {
        List<Participants> participants = participantRepository.findByStudyGroup_id(studyId);
        return participants.stream()
                .map(p -> MemberDto.fromEntity(p.getMember()))
                .toList();
    }
}
