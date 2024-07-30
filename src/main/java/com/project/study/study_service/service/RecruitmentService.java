package com.project.study.study_service.service;

import com.project.study.study_service.domain.recruitment.Recruitment;
import com.project.study.study_service.domain.recruitment.RecruitmentDto;
import com.project.study.study_service.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;


    public void registerRecruitment(RecruitmentDto.Request dto) {
        //todo: 현재 모집 중인 모집글이 있다면 등록할 수 없도록 제한할 지 말지 논의해보기
        recruitmentRepository.save(dto.toEntity());
    }

    public Page<RecruitmentDto> fetchStudyGroup(String keyword, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAllRecruitmentsWithSearch(keyword, pageable);
        return convertRecruitmentToDTO(recruitments);
    }

    public Page<RecruitmentDto> fetchStudyGroupByScheduled(String keyword, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAllRecruitmentsByScheduledWithSearch(keyword, pageable);
        return convertRecruitmentToDTO(recruitments);

    }

    public Page<RecruitmentDto> fetchStudyGroupByProgress(String keyword, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAllRecruitmentsByInProgressWithSearch(keyword, pageable);
        return convertRecruitmentToDTO(recruitments);
    }

    public Page<RecruitmentDto> fetchStudyGroupByClosed(String keyword, Pageable pageable) {
        Page<Recruitment> recruitments = recruitmentRepository.findAllRecruitmentsByClosedWithSearch(keyword, pageable);
        return convertRecruitmentToDTO(recruitments);

    }

    private Page<RecruitmentDto> convertRecruitmentToDTO(Page<Recruitment> recruitments) {
        List<RecruitmentDto> dtos = recruitments.stream()
                .map(RecruitmentDto::fromEntity)
                .toList();
        return new PageImpl<>(dtos, recruitments.getPageable(), recruitments.getTotalElements());
    }
}
