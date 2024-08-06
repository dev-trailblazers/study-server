package com.project.study.study_service.controller;


import com.project.study.authentication_service.domain.user.CustomUserDetails;
import com.project.study.study_service.domain.recruitment.RecruitmentDto;
import com.project.study.study_service.service.RecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/recruitment")
@RestController
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @GetMapping("/list")
    public ResponseEntity<Page<RecruitmentDto>> pageRecruitingStudyGroup(
            @RequestBody RecruitmentDto.SearchCondition searchCondition,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<RecruitmentDto> dtos;
        if (searchCondition.getStatus() == null) {
            dtos = recruitmentService.fetchStudyGroup(searchCondition, pageable);
            return ResponseEntity.ok(dtos);
        }
        dtos = recruitmentService.fetchStudyByStatus(searchCondition, pageable);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/new")
    public void createRecruitment(@RequestBody @Valid RecruitmentDto.Request dto,
                                  @AuthenticationPrincipal CustomUserDetails user) {
        recruitmentService.registerRecruitment(user.getId(), dto);
    }

}
