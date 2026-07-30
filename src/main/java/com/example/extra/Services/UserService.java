package com.example.extra.Services;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.User;

public interface UserService {

    UserDTO getUserById(String id);

    User getUserByEmail(String email);

    void checkUserExistenceById(String id);

    void checkUserExistenceByEmail(String email);
}
