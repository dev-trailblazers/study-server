package com.project.study.study_service.domain.recruitment;

import com.project.study.study_service.domain.studygroup.StudyGroup;
import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentDto {
    private String title;        // 모집 글 제목
    private String summary;      // 모집 글 요약 설명 (간단한 개요)
    private String details;      // 모집 글 상세 설명
    private LocalDateTime startDate; // 모집 시작일
    private LocalDateTime endDate;   // 모집 종료일
    private int maxParticipants; // 최대 모집 인원 수
    private StudyGroupDto studyGroupDto;


    public static RecruitmentDto fromEntity(Recruitment recruitment) {
        StudyGroupDto studyGroupDto = StudyGroupDto.fromEntity(recruitment.getStudyGroup());

        return RecruitmentDto.builder()
                .title(recruitment.getTitle())
                .summary(recruitment.getSummary())
                .details(recruitment.getDetails())
                .startDate(recruitment.getStartDate())
                .endDate(recruitment.getEndDate())
                .maxParticipants(recruitment.getMaxParticipants())
                .studyGroupDto(studyGroupDto)
                .build();
    }

    @Getter
    public static class Request {
        @NotBlank(message = "모집 글 제목은 필수 항목입니다.")
        private String title;
        
        @NotBlank(message = "모집 글 요약 설명은 필수 항목입니다.")
        private String summary;
        
        @NotBlank(message = "모집 글 상세 설명은 필수 항목입니다.")
        private String details;
        
        @Future(message = "모집 시작일은 현재 날짜 이후로 가능합니다.")
        private LocalDateTime startDate;

        @Future(message = "모집 종료일은 현재 날짜 이후로 가능합니다.")
        private LocalDateTime endDate;

        private int maxParticipants;

        @NotNull
        private Long studyId;


        public Recruitment toEntity() {
            return Recruitment.builder()
                    .title(title)
                    .summary(summary)
                    .details(details)
                    .startDate(startDate)
                    .endDate(endDate)
                    .maxParticipants(maxParticipants)
                    .studyGroup(new StudyGroup(studyId))
                    .build();
        }
    }

    @Getter
    public static class SearchCondition{
        private String keyword;
        private SearchType searchType;
        private RecruitmentStatus status;
    }

    public enum SearchType{
        GOAL, NAME
    }
}
