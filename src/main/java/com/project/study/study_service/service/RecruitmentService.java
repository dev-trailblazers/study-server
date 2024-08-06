package com.project.study.study_service.service;

import com.project.study.study_service.domain.recruitment.Recruitment;
import com.project.study.study_service.domain.recruitment.RecruitmentDto;
import com.project.study.study_service.domain.recruitment.RecruitmentDto.SearchCondition;
import com.project.study.study_service.repository.RecruitmentRepository;
import com.project.study.study_service.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final StudyGroupRepository studyGroupRepository;


    public void registerRecruitment(Long memberId, RecruitmentDto.Request dto) {
        studyGroupRepository.findByCreatedBy(memberId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방장만 모집글을 등록할 수 있습니다."));

        recruitmentRepository.findByCreatedByAndStudyGroupId(memberId, dto.getStudyId())
                .ifPresent(recruitment -> {
                    throw new IllegalArgumentException("이미 모집중인 스터디입니다.");
                });
        recruitmentRepository.save(dto.toEntity());
    }

    @Transactional(readOnly = true)
    public Page<RecruitmentDto> fetchStudyByStatus(SearchCondition searchCondition, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByStatusAndSearch(
                searchCondition.getKeyword(),
                searchCondition.getSearchType().name(),
                searchCondition.getStatus().name(),
                pageable);
        return recruitments.map(RecruitmentDto::fromEntity);
    }


    @Transactional(readOnly = true)
    public Page<RecruitmentDto> fetchStudyGroup(SearchCondition searchCondition, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAllRecruitmentsWithSearch(
                searchCondition.getKeyword(),
                searchCondition.getSearchType().name(),
                pageable);
       return recruitments.map(RecruitmentDto::fromEntity);
    }

}
