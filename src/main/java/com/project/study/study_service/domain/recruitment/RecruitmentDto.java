package com.project.study.study_service.domain.recruitment;

import jakarta.validation.constraints.Future;
import lombok.Getter;

import java.time.LocalDateTime;

public class RecruitmentDto {

    @Getter
    public static class Request {
        private String title;        // 모집 글 제목
        private String summary;      // 모집 글 요약 설명 (간단한 개요)
        private String details;      // 모집 글 상세 설명
        @Future(message = "모집 시작일은 현재 날짜 이후로 가능합니다.")
        private LocalDateTime startDate; // 모집 시작일
        @Future(message = "모집 종료일은 현재 날짜 이후로 가능합니다.")
        private LocalDateTime endDate;   // 모집 종료일
        private int maxParticipants; // 최대 모집 인원 수

        public Recruitment toEntity(){
            return Recruitment.builder()
                    .title(title)
                    .summary(summary)
                    .details(details)
                    .startDate(startDate)
                    .endDate(endDate)
                    .maxParticipants(maxParticipants)
                    .build();
        }
    }
}
