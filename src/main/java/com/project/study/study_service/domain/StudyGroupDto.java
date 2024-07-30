package com.project.study.study_service.domain;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyGroupDto {
    private Long id;
    private String name;
    private String description;
    private int headcount;

    public static StudyGroupDto fromEntity(StudyGroup studyGroup){
        return StudyGroupDto.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .description(studyGroup.getDescription())
                .headcount(studyGroup.getHeadCount())
                .build();
    }

    //JSON to Java Object => 기본생성자 + Getter or Setter = 둘 중 하나만 있으면 됨
    @Getter
    public static class Request {
        private String name;
        private String description;
        @Min(value = 4, message = "스터디그룹의 최소 인원은 4명입니다.")
        private int headcount;

        public StudyGroup toEntity(){
            return StudyGroup.builder()
                    .name(name)
                    .description(description)
                    .headCount(headcount)
                    .build();
        }
    }
}
