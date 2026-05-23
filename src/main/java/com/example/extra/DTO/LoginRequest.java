package com.example.extra.DTO;

import com.example.extra.Enumerator.Roles;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private Roles role;
}
