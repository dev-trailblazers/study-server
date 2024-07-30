package com.project.study.study_service.service;

import com.project.study.study_service.domain.StudyGroup;
import com.project.study.study_service.domain.StudyGroupDto;
import com.project.study.study_service.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;


    public StudyGroupDto registerStudyGroup(StudyGroupDto.Request dto){
        StudyGroup studyGroup = studyGroupRepository.save(dto.toEntity());
        return StudyGroupDto.fromEntity(studyGroup);
    }
}
