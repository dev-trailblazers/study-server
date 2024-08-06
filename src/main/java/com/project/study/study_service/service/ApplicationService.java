package com.project.study.study_service.service;

import com.project.study.study_service.domain.applications.Applications;
import com.project.study.study_service.domain.applications.ApplicationsDto;
import com.project.study.study_service.domain.participants.ParticipantRole;
import com.project.study.study_service.domain.participants.Participants;
import com.project.study.study_service.repository.ApplicationRepository;
import com.project.study.study_service.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.study.study_service.domain.applications.ApplicationStatus.*;

@Transactional
@RequiredArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ParticipantRepository participantRepository;


    public void registerApplication(ApplicationsDto.RegistrationRequest dto) {
        applicationRepository.findByMemberAndStudyAndStatus(
                        dto.getMemberId(),
                        dto.getStudyId())
                .ifPresent(application -> {
                    throw new IllegalArgumentException("이미 신청한 스터디입니다.");
                });
        Applications applications = dto.toEntity();
        applications.setStatus(WAITING);
        applicationRepository.save(applications);
    }

    public void refuseApplication(Long id, Long memberId) {
        Applications applications = validateApprovalCondition(id, memberId);
        applications.setStatus(REFUSED);
    }

    public void approveApplication(Long applicationId, Long memberId) {
        Applications applications = validateApprovalCondition(applicationId, memberId);
        applications.setStatus(APPROVED);
        participantRepository.save(Participants.builder()
                .member(applications.getMember())
                .studyGroup(applications.getStudyGroup())
                .role(ParticipantRole.ATTENDEE)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<ApplicationsDto> fetchMyApplications(ApplicationsDto.ApplicationSearchCriteria criteria,
                                                     Pageable pageable) {
        if (criteria.getStatus() == null) {
            Page<Applications> applications = applicationRepository.findAllByMemberIdAndStudyName(criteria, pageable);
            return applications.map(ApplicationsDto::fromEntity);
        }
        Page<Applications> applications = applicationRepository.findAllByMemberIdAndStudyNameAndStatus(criteria, pageable);
        return applications.map(ApplicationsDto::fromEntity);
    }


    private Applications validateApprovalCondition(Long id, Long memberId) {
        Applications application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id: " + id + "에 해당하는 신청서가 존재하지 않습니다."));
        if (application.getStudyGroup().getCreatedBy() != memberId) {
            throw new IllegalArgumentException("해당 신청에 대한 권한이 없습니다.");
        }
        if (application.getStatus() != WAITING) {
            throw new IllegalArgumentException("대기 중인 신청만 결재할 수 있습니다.");
        }
        return application;
    }
}
