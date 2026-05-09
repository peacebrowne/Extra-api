package com.example.extra.Mappers;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Select("INSERT INTO users (email, role, password) VALUES (#{email}, #{role}, #{password}) RETURNING *")
    UserDTO registerUser(User user);
}
