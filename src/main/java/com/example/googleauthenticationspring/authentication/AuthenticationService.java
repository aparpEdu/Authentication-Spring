package com.example.googleauthenticationspring.authentication;

import com.example.googleauthenticationspring.authentication.jwt.JWTAuthenticationResponse;
import com.example.googleauthenticationspring.authentication.refresh.RefreshTokenDTO;
import com.example.googleauthenticationspring.authentication.refresh.RefreshTokenService;
import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import com.example.googleauthenticationspring.authentication.jwt.JwtTokenProvider;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import com.example.googleauthenticationspring.utils.Messages;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationHelper authenticationHelper;
    private final RefreshTokenService refreshTokenService;

    public JWTAuthenticationResponse login(LoginDTO loginDto) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenProvider.generateToken(authentication.getName()));
        authenticationResponse.setRefreshToken(refreshTokenService.createRefreshToken(authentication));
        return authenticationResponse;
    }

    public String register(RegisterDTO registerDto) {
        if (userRepository.existsByEmailIgnoreCase(registerDto.email())) {
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST, Messages.EMAIL_EXISTS);
        }

        User user = buildUser(registerDto);
        userRepository.save(user);
        return Messages.USER_SUCCESSFULLY_REGISTERED;
    }

    private User buildUser(RegisterDTO registerDto) {
        User user = new User();
        user.setFirstName(registerDto.firstName());
        user.setLastName(registerDto.lastName());
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        user.setEmail(registerDto.email());
        return authenticationHelper.setRoles(user);
    }

    public String logout(RefreshTokenDTO refreshTokenDTO){
        SecurityContextHolder.clearContext();
        refreshTokenService.removeExistingRefreshToken(refreshTokenDTO.refreshToken());
        return "Successfully logged out";
    }
}
