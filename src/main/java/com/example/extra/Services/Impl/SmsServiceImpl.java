package com.example.extra.Services.Impl;

import com.aliyun.dysmsapi20180501.Client;
import com.aliyun.dysmsapi20180501.models.SendMessageWithTemplateRequest;
import com.aliyun.teautil.models.RuntimeOptions;
import com.example.extra.Configuration.Properties.SmsNotificationProperties;
import com.example.extra.Entities.Sms;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Services.SmsService;
import com.example.extra.Utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    SmsNotificationProperties smsNotificationProperties;

    @Autowired
    Client client;

    /**
     * Generates a verification code, stores it in Redis with a 5-minute expiration,
     * and sends it to the specified recipient via sms.
     *
     * @param msisdn The recipient's phone number
     * @throws InternalServerError if an error occurs during sms composition or delivery
     */
    @Override
    public void sendVerificationCode(String msisdn) {
       try {

           String key = "verification_code_" + msisdn;
           String code = Utils.generateVerificationCode();

           // Store in Redis: Key = msisdn, Value = code, Timeout = 5 seconds
           redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

           Sms sms = new Sms(msisdn, code);

           SendMessageWithTemplateRequest request = new SendMessageWithTemplateRequest()
                   .setTo(sms.getRecipient())
                   .setFrom(smsNotificationProperties.getTestSignName())
                   .setTemplateCode(smsNotificationProperties.getTestTemplateCode())
                   .setTemplateParam(sms.getMessage());

           Object response = client.sendMessageWithTemplateWithOptions(request, new RuntimeOptions());
           log.info(response.toString());

       }
       catch (Exception e){
           redisTemplate.delete("verification_code_" + msisdn);
           log.error("Internal Server Error: {}", e.getMessage(), e);
           throw new InternalServerError("Unexpected error occurred while sending sms");
       }
    }
}
