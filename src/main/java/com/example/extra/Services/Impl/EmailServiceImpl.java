package com.example.extra.Services.Impl;

import com.example.extra.Configuration.Properties.EmailNotificationProperties;
import com.example.extra.Entities.Email;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Services.EmailService;
import com.example.extra.Utils.Utils;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    EmailNotificationProperties emailNotificationProperties;


    /**
     * Generates a verification code, stores it in Redis with a 5-minute expiration,
     * and sends it to the specified recipient via email.
     *
     * @param email The recipient's email address
     * @throws InternalServerError if an error occurs during email composition or delivery
     *
     * Delete the email verification code stored in redis
     */
    @Override
    public void sendVerificationCode(String email) {
        String sender = emailNotificationProperties.getUsername();

        try {

            String key = "verification_code_" + email;
            String code = Utils.generateVerificationCode();

            // Store in Redis: Key = email, Value = code, Timeout = 5 minutes
            redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

            Email customEmail = new Email(email, code);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(customEmail.getRecipient());
            helper.setText(customEmail.getMessage(),  true);
            message.setSubject(customEmail.getSubject());

            mailSender.send(message);

        }catch (Exception e){
            redisTemplate.delete(email);

            log.error("Internal Server Error: {}", e.getMessage(), e);
            throw new InternalServerError("Unexpected error occurred while sending email");
        }
    }

}
