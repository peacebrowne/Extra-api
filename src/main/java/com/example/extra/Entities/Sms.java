package com.example.extra.Entities;


import lombok.Data;

@Data
public class Sms {
    private String recipient;
    private String message;

    public Sms(String recipient, String code) {
        this.recipient = recipient;
        this.message = """
            Your verification code is: %s, valid for 5 minutes. If this was not your operation, please ignore it. ";
            """.formatted(code);
    }
}
