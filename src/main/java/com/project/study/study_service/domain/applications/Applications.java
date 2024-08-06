package com.project.study.study_service.domain.applications;

import com.project.study.member_service.domain.member.Member;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Applications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "study_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private StudyGroup studyGroup;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) not null default 'WAITING'")
    private ApplicationStatus status;
}
