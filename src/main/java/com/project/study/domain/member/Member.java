package com.project.study.domain.member;

import com.project.study.domain.AuditingField;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Member extends AuditingField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 16, nullable = false, updatable = false)
    private String username;

    @Setter
    @Column(length = 68, nullable = false)
    private String password;

    @Column(length = 30, nullable = false, updatable = false)
    private String email;

    @Column(length = 18, nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(length = 1, nullable = false, updatable = false)
    private Gender gender;

    @Setter
    @Column(length = 2083)
    private String profile_image;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private JoinPlatform joinPlatform;

    @Setter
    @Column(columnDefinition = "boolean not null default false")
    private boolean isLocked;       //인증 오류 반복 시 계정 잠금

    @Setter
    @Column(columnDefinition = "boolean not null default true")
    private boolean useYn = true;          //계정 사용 여부


    @Builder
    public Member(Long id, String username, String password, String email, String name,
                  LocalDate birth, Gender gender, String profile_image, RoleType role,
                  JoinPlatform joinPlatform, boolean isLocked, boolean useYn, Long modifiedBy) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.birth = birth;
        this.gender = gender;
        this.profile_image = profile_image;
        this.role = role;
        this.joinPlatform = joinPlatform;
        this.isLocked = isLocked;
        this.useYn = useYn;
        super.modifiedBy = modifiedBy;
    }

    public enum Gender {
        M("남성"), F("여성");

        private final String value;

        Gender(String value) {
            this.value = value;
        }
    }

    public enum RoleType {
        ROLE_USER("일반 유저"), ROLE_ADMIN("관리자");

        private final String value;

        RoleType(String value) {
            this.value = value;
        }
    }


}