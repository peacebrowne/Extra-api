package com.example.extra.Entities;


import lombok.Data;

@Data
public class Email {
    private String recipient;
    private String message;
    private String subject;

    public Email(String recipient, String verificationCode) {
        this.recipient = recipient;
        this.subject = "Verification Code";
        this.message = """
    <div style="font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
        <h2 style="color: #333;">Welcome to Extra!</h2>
        <p>Thanks for signing up. To complete your registration, please use the verification code below:</p>
        <div style="background: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #4A90E2;">
            %s
        </div>
        <p style="font-size: 12px; color: orange; margin-top: 20px;">
           <b>Note:</b> This code will expire after 5 minutes.
        </p>
        <p style="font-size: 12px; color: #777; margin-top: 20px;">
            If you didn't request this email, you can safely ignore it.
        </p>
    </div>
    """.formatted(verificationCode);
    }
}
