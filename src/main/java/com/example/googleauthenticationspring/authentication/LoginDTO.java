package com.example.googleauthenticationspring.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginDTO(
        @Schema(example = "martin@gmail.com")
        @NotEmpty(message = "Please enter your email address")
        @Email(message = "Please enter a valid email address")
        String email,

        @Schema(example = "!Martin123")
        @NotEmpty(message = "Please enter your password")
        String password
) {
}
