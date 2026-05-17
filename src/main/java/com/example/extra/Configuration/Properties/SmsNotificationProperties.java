package com.example.extra.Configuration.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alibaba.cloud")
public class SmsNotificationProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String testSignName;
    private String testTemplateCode;
}
