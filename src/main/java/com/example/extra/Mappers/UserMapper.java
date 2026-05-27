package com.example.extra.Mappers;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE email = #{identifier} OR id = #{identifier}::UUID")
    User getUserByIdentifier(@Param("identifier") String identifier);

    @Select("INSERT INTO users (email, role, password, first_name, last_name) VALUES (#{email}, #{role}, #{password}, #{firstName}, #{lastName}) RETURNING *")
    UserDTO registerUser(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User getUserByEmail(@Param("email") String email);

    @Select("SELECT id, email, role FROM users WHERE id = #{clientId}::UUID")
    UserDTO getUserById(@Param("clientId") String clientId);
}
