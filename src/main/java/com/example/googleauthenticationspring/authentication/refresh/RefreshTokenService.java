package com.example.googleauthenticationspring.authentication.refresh;

import com.example.googleauthenticationspring.authentication.jwt.JWTAuthenticationResponse;
import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import com.example.googleauthenticationspring.exception.ResourceNotFoundException;
import com.example.googleauthenticationspring.authentication.jwt.JwtTokenProvider;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
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
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());
        existingToken.ifPresent(refreshTokenRepository::delete);

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public JWTAuthenticationResponse refreshToken(RefreshTokenDTO refreshTokenDTO){
        RefreshToken foundToken = refreshTokenRepository.findByToken(refreshTokenDTO.refreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token", "Token", refreshTokenDTO.refreshToken()));
        if(LocalDateTime.now().isAfter(foundToken.getExpiryDate())){
            throw new AuthenticationAPIException(HttpStatus.FORBIDDEN, "Refresh Token expired");
        }
        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenProvider.generateToken(foundToken.getUser().getEmail()));
        authenticationResponse.setRefreshToken(refreshTokenDTO.refreshToken());
        return authenticationResponse;
    }

    public void removeExistingRefreshToken(String token){
        RefreshToken foundToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token", "Token",token));
        refreshTokenRepository.delete(foundToken);
    }
}
