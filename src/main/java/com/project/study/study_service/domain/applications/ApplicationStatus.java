package com.project.study.study_service.domain.applications;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicationStatus {
    WAITING("대기"),
    APPROVED("승인"),
    REFUSED("반려");

    private final String description;
}
