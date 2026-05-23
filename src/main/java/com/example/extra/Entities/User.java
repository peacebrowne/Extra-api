package com.example.extra.Entities;

import com.example.extra.Enumerator.Roles;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    /*
     * ID of the user.
     * This field represents a unique identifier for the user.
     */
    private String id;

    private String firstName;
    private String lastName;

    /*
     * Email address of the user who posted the user.
     * This field is required and must be a valid email address.
     */
    @NotEmpty(message = "Please provide an email address")
    @Email(message = "Please provide a valid email address")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email format is invalid"
    )
    private String email;


    /*
     * Role of the user and should be either CLIENT or PROVIDER.
     * This field is required and should not exceed 20 characters.
     */
    @NotNull(message = "Please provide a user role")
    private Roles role;


    /*
     * Password for the user account.
     * Must contain at least one uppercase letter, one lowercase letter,
     * one number, and one special character.
     */
    @NotEmpty(message = "Password is required")
    @Size(min = 8, max = 12, message = "Password must be between 8 and 12 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;


     /*
     * One-time verification code sent to the user (e.g. email or SMS) used to verify
     * account ownership or complete sensitive actions. Expected format: exactly 6 digits.
     */
    @NotEmpty(message = "Verification is required")
    @Size(min = 6, max = 6, message = "Verification code should be 6 digits")
    @Pattern(regexp = "\\d{6}", message = "Verification code must be numeric (6 digits)")
    private String verificationCode;

    /**
     * Creation timestamp for the job.
     */
    private LocalDateTime created;


}
