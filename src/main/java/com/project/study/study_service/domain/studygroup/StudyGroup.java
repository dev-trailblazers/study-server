package com.project.study.study_service.domain.studygroup;

import com.project.study.global.jpa.AuditingField;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StudyGroup extends AuditingField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 60)
    private String name;

    @Setter
    @Column(nullable = false, length = 60)
    private String goal;

    @Setter
    @Column(nullable = false, length = 3000)
    private String description;

    @Setter
    @Column(nullable = false)
    private int headCount;


    public StudyGroup(Long id) {
        this.id = id;
    }
}
