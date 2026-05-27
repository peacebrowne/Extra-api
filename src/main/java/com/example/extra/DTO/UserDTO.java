package com.example.extra.DTO;

import com.example.extra.Enumerator.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private Roles role;
    private String firstName;
    private String lastName;
}