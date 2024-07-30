package com.project.study.study_service.controller;

import com.project.study.study_service.domain.recruitment.RecruitmentDto;
import com.project.study.study_service.domain.recruitment.RecruitmentStatus;
import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import com.project.study.study_service.service.RecruitmentService;
import com.project.study.study_service.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study-group")
@RestController
public class StudyGroupController {
    private final StudyGroupService studyGroupService;
    private final RecruitmentService recruitmentService;

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroupDto> getStudyGroup(@PathVariable Long id) {
        StudyGroupDto studyGroupDto = studyGroupService.fetchStudyGroup(id);
        return ResponseEntity.ok(studyGroupDto);
    }
    
    @GetMapping("/list")
    public ResponseEntity<Page<RecruitmentDto>> pageRecruitingStudyGroup(
            @RequestParam(required = false) RecruitmentStatus status,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<RecruitmentDto> dtos;
        if (status == null) {
            dtos = recruitmentService.fetchStudyGroup(keyword, pageable);
            return ResponseEntity.ok(dtos);
        }

        switch (status) {
            case SCHEDULED -> dtos = recruitmentService.fetchStudyGroupByScheduled(keyword, pageable);
            case IN_PROGRESS -> dtos = recruitmentService.fetchStudyGroupByProgress(keyword, pageable);
            case CLOSED -> dtos = recruitmentService.fetchStudyGroupByClosed(keyword, pageable);
            default -> throw new IllegalArgumentException(status + ": 잘못된 파라미터 입니다.");
        }
        return ResponseEntity.ok(dtos);
    }


    @PostMapping("/new")
    public ResponseEntity<StudyGroupDto> createStudyGroup(@RequestBody @Valid StudyGroupDto.Request dto) {
        StudyGroupDto studyGroupDto = studyGroupService.registerStudyGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(studyGroupDto);
    }


    @PostMapping("/recruitment")
    public void createRecruitment(@RequestBody @Valid RecruitmentDto.Request dto) {
        recruitmentService.registerRecruitment(dto);
    }
}
