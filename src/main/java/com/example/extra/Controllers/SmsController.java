package com.example.extra.Controllers;


import com.example.extra.Services.Impl.SmsEmailServiceImpl;
import com.example.extra.Success.Success;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    SmsEmailServiceImpl SmsServiceImpl;

    @PostMapping("/verification-code")
    public ResponseEntity<?> verificationCode(@RequestParam String msisdn) {
        SmsServiceImpl.sendVerificationCode(msisdn);
        return Success.OK("Successfully Sent SMS Verification Code.",  true);
    }

}
