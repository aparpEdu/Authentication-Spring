package com.example.googleauthenticationspring.authentication;

import jakarta.servlet.http.Cookie;

public interface AuthenticationService {

    String login(LoginDTO loginDTO);

    String register(RegisterDTO registerDTO);

    Cookie createCookie(String name, String value, int expiry);
}
