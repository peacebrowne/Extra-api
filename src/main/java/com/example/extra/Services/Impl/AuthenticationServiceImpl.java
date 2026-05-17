package com.example.extra.Services.Impl;

import com.example.extra.DTO.LoginRequest;
import com.example.extra.DTO.UserDTO;
import com.example.extra.DTO.UserPrincipal;
import com.example.extra.Entities.User;
import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.Conflict;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.AuthenticationMapper;
import com.example.extra.Mappers.UserMapper;
import com.example.extra.Services.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@Slf4j
public class AuthenticationServiceImpl implements UserDetailsService {


    @Autowired
    AuthenticationMapper authenticationMapper;

    @Autowired
    UserMapper userMapper;

    @Lazy
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AuthenticationServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    public UserDetails loadUserByUsername(@NonNull String email) {
        try{
            Optional<LoginRequest> userDetail = Optional.ofNullable(authenticationMapper.findLoginDetails(email));

            if (userDetail.isEmpty()){
                throw new NotFound("Invalid username or password");
            }

            return new UserPrincipal(userDetail.get());
        }  catch (NotFound e) {
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Internal Server Error: {}", e.getMessage(), e);
            throw new InternalServerError(
                    "An unexpected error occurred while fetching the user.");
        }
    }


    public String verify (LoginRequest login) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {

            User user = userMapper.findByEmail(login.getEmail());

            if (user != null) {
                return jwtService.generateToken(user);
            }
        }

        return "Fail";
    }

    public UserDTO register(User user){
        try {
            log.info("Registering User: {}", user.getEmail());
            User existingUser = userMapper.findByEmail(user.getEmail());

            if (existingUser != null) {
                throw new Conflict("User with this email: '" + user.getEmail() + "' already exists.");
            }

            boolean isVerificationCodeValid = verifyUserCode(user.getEmail(), user.getVerificationCode());

            if (!isVerificationCodeValid) {
                throw new BadRequest("Invalid verification code.");
            }

            user.setPassword(encoder.encode(user.getPassword()));

            return userMapper.registerUser(user);

        }catch (Conflict | BadRequest e){
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        }catch (Exception e){
            log.error("Internal Server Error: {}", e.getMessage(), e);
            throw new InternalServerError("An unexpected error occurred while trying to create the user");
        }
    }

    private boolean verifyUserCode(String email, String verificationCode) {
        // Retrieve the code from Redis using the email as the key
        String key = "verification_code_" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        log.info("\n\n Verifying user code: {}, {} \n\n", savedCode, verificationCode);

        return verificationCode.equals(savedCode);
    }

}
