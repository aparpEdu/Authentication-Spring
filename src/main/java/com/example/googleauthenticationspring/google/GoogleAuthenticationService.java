package com.example.googleauthenticationspring.google;

import org.springframework.web.client.RestTemplate;

public interface GoogleAuthenticationService {
    String googleSignIn(String idToken, RestTemplate restTemplate);
    String buildGoogleAuthURL();
}
