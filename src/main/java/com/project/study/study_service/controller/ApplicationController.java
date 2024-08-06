package com.project.study.study_service.controller;

import com.project.study.authentication_service.domain.user.CustomUserDetails;
import com.project.study.study_service.domain.applications.ApplicationStatus;
import com.project.study.study_service.domain.applications.ApplicationsDto;
import com.project.study.study_service.domain.applications.ApplicationsDto.RegistrationRequest;
import com.project.study.study_service.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/application")
@RestController
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping("/new/{studyId}")
    public ResponseEntity<Void> createApplication(@PathVariable Long studyId,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        applicationService.registerApplication(RegistrationRequest.builder()
                .memberId(user.getId())
                .studyId(studyId)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{applicationId}/{status}")
    public ResponseEntity<Void> updateApplication(@PathVariable Long applicationId,
                                                  @PathVariable ApplicationStatus status,
                                                  @AuthenticationPrincipal CustomUserDetails user) {
        return switch (status) {
            case APPROVED -> {
                applicationService.approveApplication(applicationId, user.getId());
                yield ResponseEntity.status(HttpStatus.CREATED).build();
            }
            case REFUSED -> {
                applicationService.refuseApplication(applicationId, user.getId());
                yield ResponseEntity.status(HttpStatus.OK).build();
            }
            default -> throw new IllegalArgumentException("승인 혹은 반려 상태로만 변경할 수 있습니다.");
        };
    }

    @GetMapping("/list")
    public ResponseEntity<Page<ApplicationsDto>> pageMyApplications(
            @RequestBody @Valid ApplicationsDto.ApplicationSearchCriteria criteria,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {
        criteria.setMemberId(user.getId());
        Page<ApplicationsDto> applicationsDtos = applicationService.fetchMyApplications(criteria, pageable);
        return ResponseEntity.ok(applicationsDtos);
    }
}
