package com.project.study.study_service.domain.participants;

import com.project.study.global.jpa.AuditingField;
import com.project.study.member_service.domain.member.Member;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Participants extends AuditingField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "study_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) not null default 'ATTENDEE'")
    private ParticipantRole role;
}