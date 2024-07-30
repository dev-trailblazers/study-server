package com.project.study.study_service.domain.recruitment;

import com.project.study.global.jpa.AuditingField;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Recruitment extends AuditingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;   // 모집 글 제목

    @Column(nullable = false, length = 500)
    private String summary; // 모집 글 요약 설명 (간단한 개요)

    @Column(nullable = false, length = 5000)
    private String details; // 모집 글 상세 설명

    @Column(nullable = false)
    private LocalDateTime startDate;    // 모집 시작일

    @Column(nullable = false)
    private LocalDateTime endDate;      // 모집 종료일

    @Column(nullable = false)
    private int maxParticipants;    // 최대 모집 인원 수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;  // 연관된 스터디 그룹

}

