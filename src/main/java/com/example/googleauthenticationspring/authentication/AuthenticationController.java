package com.example.googleauthenticationspring.authentication;

import com.example.googleauthenticationspring.authentication.jwt.JWTAuthenticationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Login and Register REST APIs for Authentication Resource")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @Operation(
            summary = "Login User REST API",
            description = "Login User REST API is used to get user's bearer token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JWTAuthenticationResponse> login(@Valid @RequestBody LoginDTO loginDto) {
        return ResponseEntity.ok(authenticationService.login(loginDto));
    }


    @Operation(
            summary = "Register User REST API",
            description = "Register User REST API is used to save user into database"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 CREATED"
    )
    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDTO registerDto){
        String response = authenticationService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @SecurityRequirement(
            name = "Bearer Authentication"
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("home")
    public ResponseEntity<String> welcome(Authentication authentication){
        return ResponseEntity.ok(authentication.getName());
    }

    @Operation(
            summary = "Refresh Token REST API",
            description = "Refresh Token REST API is used to generate a new access token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    @PostMapping("refresh")
    @SecurityRequirement(
            name = "Bearer Authentication"
    )
    public ResponseEntity<JWTAuthenticationResponse> refreshToken(Authentication authentication){
        return ResponseEntity.ok(authenticationService.refreshToken(authentication));
    }

    @Operation(
            summary = "Logout REST API",
            description = "Logout REST API is used to log the user out of the system"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    @PostMapping("logout")
    public ResponseEntity<String> logout(){
        return ResponseEntity.ok(authenticationService.logout());
    }
}
