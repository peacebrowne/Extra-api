package com.example.extra.Services;

import com.example.extra.Entities.Email;

public interface EmailService {
    void sendVerificationCode(Email email);
}
