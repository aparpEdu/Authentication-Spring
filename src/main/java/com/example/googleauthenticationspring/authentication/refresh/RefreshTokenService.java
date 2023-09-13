package com.example.googleauthenticationspring.authentication.refresh;

import com.example.googleauthenticationspring.authentication.jwt.JWTAuthenticationResponse;
import com.example.googleauthenticationspring.authentication.jwt.JwtAuthenticationFilter;
import com.example.googleauthenticationspring.authentication.jwt.JwtTokenProvider;
import com.example.googleauthenticationspring.authentication.jwt.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter authenticationFilter;

    public JWTAuthenticationResponse refreshToken(HttpServletRequest request){
        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authenticationResponse.setAccessToken(jwtTokenProvider.generateToken(authentication.getName(), TokenType.ACCESS));
        authenticationResponse.setRefreshToken(authenticationFilter.getTokenFromRequest(request));
        return authenticationResponse;
    }
}
