package com.project.study.study_service.domain.participants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ParticipantRole {
    ATTENDEE("일반 참가자"),
    MANAGER("매니저"),
    LEADER("방장");

    private final String description;
}
