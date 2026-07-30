package com.example.extra.Configuration;

import com.aliyun.dysmsapi20180501.Client;
import com.aliyun.teaopenapi.models.Config;
import com.example.extra.Configuration.Properties.SmsNotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SmsConfiguration {

    private final SmsNotificationProperties smsNotificationProperties;

    @Bean
    public Client createClient() throws Exception {
        Config configuration = new Config()
                .setAccessKeyId(smsNotificationProperties.getAccessKeyId())
                .setAccessKeySecret(smsNotificationProperties.getAccessKeySecret())
                .setEndpoint("dysmsapi.ap-southeast-1.aliyuncs.com");

        return new Client(configuration);

    }
}
