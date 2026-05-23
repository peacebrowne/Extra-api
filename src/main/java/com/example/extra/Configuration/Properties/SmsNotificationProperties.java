package com.example.extra.Configuration.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alibaba.cloud")
public class SmsNotificationProperties {
    /**
     * Alibaba Cloud Access Key ID.
     * Maps to property 'alibaba.cloud.accessKeyId'.
     */
    private String accessKeyId;

    /**
     * Alibaba Cloud Access Key Secret.
     * Maps to property 'alibaba.cloud.accessKeySecret'.
     */
    private String accessKeySecret;

    /**
     * SMS sign name used for testing (sign name registered in Alibaba Cloud).
     * Maps to property 'alibaba.cloud.testSignName'.
     */
    private String testSignName;

    /**
     * SMS template code used for testing (template code from Alibaba Cloud SMS).
     * Maps to property 'alibaba.cloud.testTemplateCode'.
     */
    private String testTemplateCode;
}