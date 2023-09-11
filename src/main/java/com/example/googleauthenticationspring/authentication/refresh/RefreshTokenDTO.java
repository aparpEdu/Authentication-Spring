package com.example.googleauthenticationspring.authentication.refresh;

import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenDTO (
        @NotEmpty
        String refreshToken
){
}
