package com.project.study.service;

import com.project.study.domain.member.JoinPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OAuth2Service {
    private final RestTemplate restTemplate;

    //RestTemplate는 RestTemplateBuilder를 통해 주입받는다.
    public OAuth2Service(@Autowired RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void logout(String accessToken, JoinPlatform joinPlatform) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        restTemplate.exchange(
                joinPlatform.getLogoutUrl(),
                HttpMethod.POST,
                entity,
                Long.class);
    }
}
