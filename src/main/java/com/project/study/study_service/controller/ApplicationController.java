package com.project.study.study_service.controller;

import com.project.study.authentication_service.domain.user.CustomUserDetails;
import com.project.study.study_service.domain.applications.ApplicationStatus;
import com.project.study.study_service.domain.applications.ApplicationsDto;
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
        ApplicationsDto.RegistrationRequest dto = ApplicationsDto.RegistrationRequest.builder()
                .memberId(user.getId())
                .studyId(studyId)
                .build();
        applicationService.registerApplication(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{applicationId}/{status}")
    public void updateApplication(@PathVariable Long applicationId,
                                  @PathVariable ApplicationStatus status,
                                  @AuthenticationPrincipal CustomUserDetails user) {
        applicationService.updateApplicationStatus(applicationId, user.getId(), status);
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
