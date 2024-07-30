package com.project.study.study_service.service;

import com.project.study.study_service.domain.recruitment.RecruitmentDto;
import com.project.study.study_service.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;


    public void registerRecruitment(RecruitmentDto.Request dto){
        //todo: 현재 모집 중인 모집글이 있다면 등록할 수 없도록 제한할 지 말지 논의해보기
        recruitmentRepository.save(dto.toEntity());
    }
}
