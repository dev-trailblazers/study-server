package com.project.study.study_service.domain.recruitment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStatus {
    SCHEDULED("모집 예정"),
    IN_PROGRESS("모집 중"),
    CLOSED("모집 마감"),
    ;

    private final String description;
}