package com.project.study.study_service.controller;

import com.project.study.authentication_service.domain.user.CustomUserDetails;
import com.project.study.member_service.domain.member.MemberDto;
import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import com.project.study.study_service.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
@RestController
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @GetMapping("/detail/{id}")
    public ResponseEntity<StudyGroupDto> getStudyGroup(@PathVariable Long id) {
        StudyGroupDto studyGroupDto = studyGroupService.fetchStudyGroup(id);
        return ResponseEntity.ok(studyGroupDto);
    }


    @PostMapping("/new")
    public ResponseEntity<StudyGroupDto> createStudyGroup(@RequestBody @Valid StudyGroupDto.Request dto) {
        StudyGroupDto studyGroupDto = studyGroupService.registerStudyGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(studyGroupDto);
    }

    @Transactional
    @GetMapping("/my")
    public ResponseEntity<List<StudyGroupDto>> getMyStudyGroups(@AuthenticationPrincipal CustomUserDetails user) {
        List<StudyGroupDto> result = studyGroupService.fetchMyStudyGroups(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/participants/{studyId}")
    public ResponseEntity<List<MemberDto>> getParticipants(@PathVariable Long studyId) {
        List<MemberDto> result = studyGroupService.fetchParticipantsByStudyId(studyId);
        return ResponseEntity.ok(result);
    }

}
