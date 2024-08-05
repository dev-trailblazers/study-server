package com.project.study.study_service.domain.recruitment;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RecruitmentStatusEnumConverter implements Converter<String, RecruitmentStatus> {

    @Override
    public RecruitmentStatus convert(String source) {
        return RecruitmentStatus.valueOf(source.toUpperCase());
    }
}
