package com.project.study.authentication_service.service;

import com.project.study.member_service.domain.email.EmailVerify;
import com.project.study.member_service.domain.email.EmailVerifyDto;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final String SENDER_EMAIL;
    private final RedisTemplate redisTemplate;

    public EmailService(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String SENDER_EMAIL,
                        RedisTemplate redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.SENDER_EMAIL = SENDER_EMAIL;
        this.redisTemplate = redisTemplate;
    }


    public void sendVerificationCode(String email) {
        String code = generateVerificationCode();
        MimeMessage message = generateEmailForVerify(email, code);
        javaMailSender.send(message);

        saveEmailAuthentication(new EmailVerify(email, code));
    }

    public boolean checkVerificationCodeForEmail(EmailVerifyDto dto) {
        EmailVerify auth = fetchByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증 요청이 존재하지 않습니다."));

        String code = auth.getCode();
        if (code.equals(dto.code())) {
            auth.setStatus(true);
            saveEmailAuthentication(auth);
            return true;
        }
        return false;
    }

    public Optional<EmailVerify> fetchByEmail(String email) {
        return Optional.ofNullable((EmailVerify) redisTemplate.opsForValue().get(email));
    }


    private MimeMessage generateEmailForVerify(String mail, String verificationCode) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(SENDER_EMAIL);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("StudyRoom 이메일 인증을 요청하셨습니다.");
            String body = String.format("""
                    <body>
                      <h2 style="color: #587FA7; padding: 10px; text-align: left;">StudyRoom</h2>
                      <div style="width:500px; margin-left: 10px; border: 0.5px solid gray; border-radius: 10px;">
                        <h2 style="text-align: center;">StudyRoom 이메일 인증</h2>
                        <div style="padding: 10px; margin: 10px; line-height: 1.5;">
                          <strong>StudyRoom 회원가입을 위한 이메일 인증을 요청하였습니다.</strong>
                          <br><br>
                          StudyRoom 회원 가입을 하기 위해 사용됩니다.<br>
                          아래 코드를 회원가입 창으로 돌아가 이메일 인증을 진행해주세요.<br>
                        </div>
                        <div style="padding: 10px; margin: 10px; border: 1px solid lightgray; border-radius: 10px;">
                          <span style="font-weight: bold;">인증번호:</span>
                          <span style="color:#587FA7; font-weight: bold;">%s</span>
                        </div>
                      </div>
                    </body>
                    """, verificationCode);
            message.setText(body, "UTF-8", "html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1_000_000));
    }

    private void saveEmailAuthentication(EmailVerify emailVerify) {
        redisTemplate.opsForValue().set(emailVerify.getEmail(), emailVerify, 5, TimeUnit.MINUTES);
    }
}
