package com.project.study.study_service.domain.applications;

import com.project.study.member_service.domain.member.Member;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ApplicationsDto {
    private Long id;
    private Long memberId;
    private Long studyId;
    private String status;

    public static ApplicationsDto fromEntity(Applications applications) {
        return ApplicationsDto.builder()
                .id(applications.getId())
                .memberId(applications.getMember().getId())
                .studyId(applications.getStudyGroup().getId())
                .status(applications.getStatus().name())
                .build();
    }

    @Getter
    @Builder
    public static class RegistrationRequest {
        private Long memberId;

        private Long studyId;

        //todo: 스터디 신청 정보 추가

        public Applications toEntity() {
            return Applications.builder()
                    .member(new Member(memberId))
                    .studyGroup(new StudyGroup(studyId))
                    .build();
        }
    }

    @Getter
    public static class ApplicationSearchCriteria {
        @Setter
        private Long memberId;
        private String studyName;
        private ApplicationStatus status;
    }
}
