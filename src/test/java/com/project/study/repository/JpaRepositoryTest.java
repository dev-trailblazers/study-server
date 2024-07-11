package com.project.study.repository;

import com.project.study.domain.member.JoinPlatform;
import com.project.study.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JPA 테스트")
@ActiveProfiles("test")
@DataJpaTest
class JpaRepositoryTest {

    private final MemberRepository memberRepository;

    public JpaRepositoryTest(@Autowired MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @DisplayName("[JPA] - 회원 저장")
    @Test
    void save_member() {
        Member saved = memberRepository.save(createTestMember());
        assertThat(saved).isNotNull();
    }

    @DisplayName("[JPA] - 접속 가능한 회원 조회")
    @Test
    void inquiry_available_member() {
        Member savedMember = createTestMember();
        memberRepository.save(savedMember);

        Optional<Member> member = memberRepository.findAvailableMemberByUsername(savedMember.getUsername());
        assertThat(member).isPresent();
    }


    @DisplayName("[JPA] - 접속 가능한 회원 조회")
    @MethodSource("unavailableMembers")
    @ParameterizedTest(name = "{1}")
    void inquiry_unavailable_member(Member savedMember, String displayName) {
        memberRepository.save(savedMember);

        Optional<Member> member = memberRepository.findAvailableMemberByUsername(savedMember.getUsername());
        assertThat(member).isEmpty();
    }



    private static Member createTestMember(){
        return Member.builder()
                .username("tester")
                .password("qwe123")
                .email("test123@example.com")
                .name("홍길동")
                .birth(LocalDate.of(1999,11,23))
                .gender(Member.Gender.M)
                .profile_image("test_img.com")
                .role(Member.RoleType.ROLE_USER)
                .joinPlatform(JoinPlatform.BASIC)
                .build();
    }

    static Stream<Arguments> unavailableMembers(){
        Member lockedMember = createTestMember();
        lockedMember.setLocked(true);

        Member removedMember = createTestMember();
        removedMember.setUseYn(false);

        return Stream.of(
                Arguments.of(lockedMember, "계정 잠금 상태인 회원"),
                Arguments.of(removedMember, "삭제된 상태인 회원")
        );
    }

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {
        @Bean
        public AuditorAware<Long> auditorAware() {
            return () -> Optional.ofNullable(1L);
        }
    }
}