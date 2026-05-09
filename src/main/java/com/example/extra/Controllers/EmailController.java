package com.example.extra.Controllers;

import com.example.extra.Entities.Email;
import com.example.extra.Services.Impl.EmailServiceImpl;
import com.example.extra.Success.Success;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class EmailController {

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @PostMapping("/verification-code")
    public ResponseEntity<?> send(@RequestBody Email email) {
        emailServiceImpl.sendVerificationCode(email);
        return Success.OK("Successfully Sent Mail.",  true);
    }

}
