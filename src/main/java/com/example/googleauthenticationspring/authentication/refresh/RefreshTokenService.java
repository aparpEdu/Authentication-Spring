package com.example.googleauthenticationspring.authentication.refresh;

import com.example.googleauthenticationspring.authentication.jwt.JWTAuthenticationResponse;
import com.example.googleauthenticationspring.authentication.jwt.JwtTokenProvider;
import com.example.googleauthenticationspring.exception.ResourceNotFoundException;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String createRefreshToken(Authentication authentication){
        RefreshToken refreshToken = new RefreshToken();
        User user = userRepository.findUserByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User","Email", authentication.getName()));

        refreshToken.setUserId(user.getId());
        refreshToken.setExpirySeconds(86400L);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public JWTAuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO){
        RefreshToken foundToken = refreshTokenRepository.findById(refreshTokenDTO.refreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token", "Token", refreshTokenDTO.refreshToken()));

        User user = userRepository.findById(foundToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User","Id", foundToken.getUserId()));

        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenProvider.generateToken(user.getEmail()));
        authenticationResponse.setRefreshToken(refreshTokenDTO.refreshToken());
        return authenticationResponse;
    }

    public void removeExistingRefreshToken(String token){
        RefreshToken foundToken = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token", "Token",token));
        refreshTokenRepository.delete(foundToken);
    }
}
