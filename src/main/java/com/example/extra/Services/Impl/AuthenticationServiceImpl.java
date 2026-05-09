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
    public UserDetails loadUserByUsername(@NonNull String username) {
        try{
            Optional<LoginRequest> userDetail = Optional.ofNullable(authenticationMapper.findLoginDetails(username));

            if (userDetail.isEmpty()){
                throw new NotFound("Invalid username or password");
            }

            return new UserPrincipal(userDetail.get());
        }  catch (NotFound e) {
            throw e;
        } catch (Exception e) {
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
                return jwtService.generateToken(login.getEmail());
            }
        }


        return "Fail";
    }

    public UserDTO register(User user){
        try {
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
            throw e;
        }catch (Exception e){
            System.out.println("\n\n" + e.toString() + "\n\n");
            throw new InternalServerError("An unexpected error occurred while trying to create the user");
        }
    }

    public boolean verifyUserCode(String email, String verificationCode) {
        // Retrieve the code from Redis using the email as the key
        String savedCode = redisTemplate.opsForValue().get(email);

        return verificationCode.equals(savedCode);
    }

}
