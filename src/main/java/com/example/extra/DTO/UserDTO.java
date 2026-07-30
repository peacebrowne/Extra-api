package com.example.extra.DTO;

import com.example.extra.Entities.Location;
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
    private String fullName;
    private String imageUrl;
    private String msisdn;
    private Location location;
}