package com.example.extra.Services.Impl;

import com.example.extra.DTO.LoginRequest;
import com.example.extra.Entities.User;
import com.example.extra.Enumerator.Roles;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Mappers.AuthenticationMapper;
import com.example.extra.Mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {


    @Mock
    UserMapper userMapper;

    @Mock
    AuthenticationMapper authenticationMapper;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    AuthenticationServiceImpl authenticationServiceImpl;

    LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("echoes832@gmail.com");
        loginRequest.setPassword("Test@123");
        loginRequest.setRole(Roles.CLIENT);
    }

    @Test
    void verify_WhenRoleDoesNotMatch_ThrowsBadRequestException() {
        User user = new User();
        user.setEmail("echoes832@gmail.com");
        user.setPassword("Test@123");
        user.setRole(Roles.PROVIDER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userMapper.getUserByEmail(loginRequest.getEmail())).thenReturn(user);

        // Act & Assert
        assertThatThrownBy(() -> authenticationServiceImpl.verify(loginRequest))
                .isInstanceOf(BadRequest.class)
                .hasMessage("Invalid role for the provided email.");

    }




}