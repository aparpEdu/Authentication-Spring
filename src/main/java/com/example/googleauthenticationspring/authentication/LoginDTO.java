package com.example.googleauthenticationspring.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginDTO(
        @NotEmpty(message = "Please enter your email address")
        @Email(message = "Please enter a valid email address")
        String email,

        @NotEmpty(message = "Please enter your password")
        String password
) {
}
