package com.project.study.study_service.controller;

import com.project.study.study_service.domain.studygroup.StudyGroupDto;
import com.project.study.study_service.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study-group")
@RestController
public class StudyGroupController {
    private final StudyGroupService studyGroupService;

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroupDto> getStudyGroup(@PathVariable Long id){
        StudyGroupDto studyGroupDto = studyGroupService.fetchStudyGroup(id);
        return ResponseEntity.ok(studyGroupDto);
    }


    @PostMapping("/new")
    public ResponseEntity<StudyGroupDto> createStudyGroup(@RequestBody @Valid StudyGroupDto.Request dto){
        StudyGroupDto studyGroupDto = studyGroupService.registerStudyGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(studyGroupDto);
    }

}
