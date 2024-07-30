package com.project.study.study_service.controller;

import com.project.study.study_service.domain.StudyGroupDto;
import com.project.study.study_service.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study-group")
@RestController
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @PostMapping("/new")
    public ResponseEntity<StudyGroupDto> createStudyGroup(@RequestBody @Valid StudyGroupDto.Request dto){
        StudyGroupDto studyGroupDto = studyGroupService.registerStudyGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(studyGroupDto);
    }


}
