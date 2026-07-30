package com.example.extra.Controllers;


import com.example.extra.Services.Impl.UserServiceImpl;
import com.example.extra.Success.Success;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
@RequiredArgsConstructor
public class UsersController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return Success.OK("Successfully retrieved User",  userServiceImpl.getUserById(id));
    }
}
