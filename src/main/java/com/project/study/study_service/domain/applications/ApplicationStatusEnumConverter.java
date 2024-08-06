package com.project.study.study_service.domain.applications;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusEnumConverter implements Converter<String, ApplicationStatus> {

    @Override
    public ApplicationStatus convert(String source) {
        return ApplicationStatus.valueOf(source.toUpperCase());
    }
}
