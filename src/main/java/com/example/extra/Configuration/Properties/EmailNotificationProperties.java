package com.example.extra.Configuration.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.mail")
public class EmailNotificationProperties {
    /**
     * The username (email address) used to authenticate with the mail server.
     * Maps to property 'spring.mail.username'.
     */
    private String username;
}