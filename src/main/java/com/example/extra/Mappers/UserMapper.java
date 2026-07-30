package com.example.extra.Mappers;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE email = #{identifier} OR id = #{identifier}::UUID")
    User getUserByIdentifier(@Param("identifier") String identifier);

    @Select("INSERT INTO users (email, role, password, full_name, msisdn, address, latitude, longitude) VALUES (#{email}, #{role}, #{password}, #{fullName}, #{msisdn}, #{address}, #{latitude}, #{longitude}) RETURNING *")
    UserDTO registerUser(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User getUserByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE id = #{clientId}::UUID")
    UserDTO getUserById(@Param("clientId") String clientId);

    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE id = #{id}::UUID)")
    boolean checkUserExistenceById(@Param("id") String id);

    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE email = #{email})")
    boolean checkUserExistenceByEmail(@Param("email") String email);
}
