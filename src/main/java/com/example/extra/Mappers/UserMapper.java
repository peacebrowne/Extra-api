package com.example.extra.Mappers;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE email = #{email}")
    User getUserByEmail(@Param("email") String email);

    @Select("INSERT INTO users (email, role, password) VALUES (#{email}, #{role}, #{password}) RETURNING *")
    UserDTO registerUser(User user);

    @Select("SELECT id, email, role FROM users WHERE id = #{clientId}::UUID")
    UserDTO getUserById(@Param("clientId") String clientId);
}
