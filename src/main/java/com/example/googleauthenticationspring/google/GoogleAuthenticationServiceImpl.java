package com.example.googleauthenticationspring.google;

import com.example.googleauthenticationspring.authentication.AuthenticationHelper;
import com.example.googleauthenticationspring.exception.ResourceNotFoundException;
import com.example.googleauthenticationspring.security.JwtTokenProvider;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@AllArgsConstructor
@Service
public class GoogleAuthenticationServiceImpl implements GoogleAuthenticationService{

    private final UserRepository userRepository;
    private final AuthenticationHelper authenticationHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final GoogleApi googleApi;

    @Override
    public String googleSignIn(String token, RestTemplate restTemplate) {
        GoogleUserDTO googleUserDto = googleApi.fetchUserInfo(restTemplate, token);
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = new BCryptPasswordEncoder().encode(randomPassword);

        User user = new User();

        if(userRepository.existsByEmailIgnoreCase(googleUserDto.email())){
            user = userRepository.findUserByEmailIgnoreCase(googleUserDto.email())
                    .orElseThrow( () ->new ResourceNotFoundException("User", "Email", googleUserDto.email()));
        }
        user.setEmail(googleUserDto.email());
        user.setFirstName(googleUserDto.given_name());
        user.setLastName(googleUserDto.last_name());
        user.setPassword(encodedPassword);
        user.setIsVerified(true);
        userRepository.save(authenticationHelper.setRoles(user));

        user = userRepository.findUserByEmailIgnoreCase(googleUserDto.email())
                .orElseThrow( () ->new ResourceNotFoundException("User", "Email", googleUserDto.email()));


        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), randomPassword));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);

    }

    @Override
    public String buildGoogleAuthURL() {
        ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");

        String authorizationUri = googleRegistration.getProviderDetails().getAuthorizationUri();
        String redirectUri = UriComponentsBuilder.fromHttpUrl(googleRegistration.getRedirectUri()).build().encode().toUriString();
        String clientId = googleRegistration.getClientId();
        String scopes = "openid profile email https://www.googleapis.com/auth/user.birthday.read";
        return UriComponentsBuilder.fromHttpUrl(authorizationUri)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("prompt", "consent")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scopes)
                .build().encode().toUriString();
    }


}
