package com.project.study.study_service.service;

import com.project.study.study_service.domain.studygroup.StudyGroup;
import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import com.project.study.study_service.repository.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;


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
}
