package com.example.extra.Mappers;

import com.example.extra.DTO.LoginRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthenticationMapper {

    @Select("SELECT email, password, role FROM users WHERE email = #{email}")
    LoginRequest findLoginDetails(@Param("email") String email);

}
