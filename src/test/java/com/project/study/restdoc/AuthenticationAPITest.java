package com.project.study.restdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.study.authentication_service.controller.AuthenticationController;
import com.project.study.authentication_service.service.AuthenticationService;
import com.project.study.authentication_service.service.EmailService;
import com.project.study.member_service.domain.email.EmailVerifyDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthenticationController.class)
@AutoConfigureRestDocs(outputDir = "build/snippets")
public class AuthenticationAPITest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private EmailService emailService;

    @Test
    void name() throws Exception {
        EmailVerifyDto dto = EmailVerifyDto.builder()
                .email("test@email.com")
                .code("123456")
                .build();

        when(emailService.checkVerificationCodeForEmail(any())).thenReturn(true);

        this.mockMvc.perform(post("/api/v1/auth/verify/email")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf())
                        .with(user("testUser").roles("USER"))  // 인증된 사용자로 설정
                ).andExpect(status().isOk())
                .andDo(document("verify-email",
                        requestFields(
                                fieldWithPath("email").description("The email address to verify"),
                                fieldWithPath("code").description("The verification code")
                        )
                ));
    }


}
