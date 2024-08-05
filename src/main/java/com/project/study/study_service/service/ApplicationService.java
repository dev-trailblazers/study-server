package com.project.study.study_service.service;

import com.project.study.study_service.domain.applications.ApplicationStatus;
import com.project.study.study_service.domain.applications.Applications;
import com.project.study.study_service.domain.applications.ApplicationsDto;
import com.project.study.study_service.domain.studygroup.StudyGroup;
import com.project.study.study_service.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.study.study_service.domain.applications.ApplicationStatus.WAITING;

@Transactional
@RequiredArgsConstructor
@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

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

    public void updateApplicationStatus(Long id, Long memberId, ApplicationStatus status) {
        Applications application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id: " + id + "에 해당하는 신청서가 존재하지 않습니다."));

        StudyGroup studyGroup = application.getStudyGroup();
        //todo: 추후에는 방장으로 검사가 아닌 참가자 권한으로 구분해야할 수 있음
        if(studyGroup.getCreatedBy() != memberId) {
            throw new IllegalArgumentException("해당 신청서에 대한 권한이 없습니다.");
        }
        //WAITING 상태에서만 변경할 수 있음
        if(application.getStatus() == WAITING && status != WAITING) {
            application.setStatus(status);
        }
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
}
