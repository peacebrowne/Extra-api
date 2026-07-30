package com.example.extra.Controllers;


import com.example.extra.DTO.LoginRequest;
import com.example.extra.Entities.User;
import com.example.extra.Services.Impl.AuthenticationServiceImpl;
import com.example.extra.Success.Success;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return Success.OK("Login Successful", authenticationServiceImpl.verify(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        return Success.CREATED("Successfully created User", authenticationServiceImpl.register(user));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return Success.OK("Successfully Retrieved All Users", true);
    }


}
