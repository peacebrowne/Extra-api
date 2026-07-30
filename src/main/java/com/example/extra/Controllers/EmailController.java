package com.example.extra.Controllers;

import com.example.extra.Services.Impl.EmailEmailServiceImpl;
import com.example.extra.Success.Success;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class EmailController {

    private final EmailEmailServiceImpl emailServiceImpl;

    @PostMapping("/verification-code")
    public ResponseEntity<?> send(@RequestParam String email) {
        emailServiceImpl.sendVerificationCode(email);
        return Success.OK("Successfully Sent Mail Verification Code.",  true);
    }

}
