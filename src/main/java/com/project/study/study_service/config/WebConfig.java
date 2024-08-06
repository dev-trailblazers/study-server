package com.project.study.study_service.config;

import com.project.study.study_service.domain.applications.ApplicationStatusEnumConverter;
import com.project.study.study_service.domain.recruitment.RecruitmentStatusEnumConverter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new RecruitmentStatusEnumConverter());
        registry.addConverter(new ApplicationStatusEnumConverter());
    }
}
