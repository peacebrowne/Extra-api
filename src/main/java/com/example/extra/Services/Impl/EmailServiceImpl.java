package com.example.extra.Services.Impl;

import com.example.extra.DTO.VerificationCode;
import com.example.extra.Entities.Email;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Services.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String sender;


    @Override
    public void sendVerificationCode(Email email) {
        try {

            String generateVerificationCode = generateVerificationCode();

            // Store in Redis: Key = email, Value = code, Timeout = 5 minutes
            redisTemplate.opsForValue().set(email.getRecipient(), generateVerificationCode, 5, TimeUnit.MINUTES);

            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(generateVerificationCode);

            Email customEmail = new Email(email.getRecipient(), generateVerificationCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(customEmail.getRecipient());
            helper.setText(customEmail.getMessage(),  true);
            message.setSubject(customEmail.getSubject());

            mailSender.send(message);

        }catch (Exception e){
            System.out.println("Email Send Failed " + e.getMessage());
            throw new InternalServerError("Unexpected error occurred while sending email");
        }
    }

    private String generateVerificationCode() {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            number.append(digit);
        }
        return number.toString();
    }
}
