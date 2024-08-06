package com.project.study.study_service.domain.studygroup;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyGroupDto {
    private Long id;
    private String name;
    private String goal;
    private String description;
    private int headcount;

    public static StudyGroupDto fromEntity(StudyGroup studyGroup){
        return StudyGroupDto.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .goal(studyGroup.getGoal())
                .description(studyGroup.getDescription())
                .headcount(studyGroup.getHeadCount())
                .build();
    }

    //JSON to Java Object => 기본생성자 + Getter or Setter = 둘 중 하나만 있으면 됨
    @Getter
    public static class Request {
        @NotBlank(message = "스터디그룹 이름은 필수 항목입니다.")
        private String name;

        @NotBlank(message = "스터디에 대한 설명은 필수 항목입니다.")
        private String description;

        @NotBlank(message = "스터디 목표는 필수 항목입니다.")
        private String goal;

        @Min(value = 4, message = "스터디그룹의 최소 인원은 4명입니다.")
        private int headcount;

        public StudyGroup toEntity(){
            return StudyGroup.builder()
                    .name(name)
                    .goal(goal)
                    .description(description)
                    .headCount(headcount)
                    .build();
        }
    }
}
