package com.example.googleauthenticationspring.google;

import com.example.googleauthenticationspring.authentication.AuthenticationService;
import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class GoogleAuthenticationController {

    private final GoogleAuthenticationService googleAuthenticationService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthenticationService authenticationService;
    private final GoogleApi googleApi;


    @Operation(
            summary = "Initiate Google Authentication REST API",
            description = "Initiate Google Authentication REST API is used to return Google authorization url"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    @GetMapping("/initiate-google")
    public ResponseEntity<String> initiateGoogleOAuth() {
        return ResponseEntity.ok(googleAuthenticationService.buildGoogleAuthURL());
    }

    @GetMapping("/login/oauth2/code/google")
    @Hidden
    public ResponseEntity<String> handleGoogleCallback(@RequestParam String code, HttpServletResponse response) {
        RestTemplate restTemplate = new RestTemplate();

        ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(googleRegistration.getClientId(), googleRegistration.getClientSecret());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        requestBody.put(OAuth2ParameterNames.CODE, code);
        requestBody.put(OAuth2ParameterNames.REDIRECT_URI, googleRegistration.getRedirectUri());
        requestBody.put(OAuth2ParameterNames.CLIENT_ID, googleRegistration.getClientId());requestBody.put(OAuth2ParameterNames.CLIENT_SECRET, googleRegistration.getClientSecret());
        requestBody.put(OAuth2ParameterNames.SCOPE, "openid profile email https://www.googleapis.com/auth/user.birthday.read");HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    googleRegistration.getProviderDetails().getTokenUri(),
                    request,
                    Map.class
            );
        String accessToken = (String) Objects.requireNonNull(tokenResponse.getBody()).get("access_token");
        try {
            googleApi.fetchUserBirthdate(restTemplate,accessToken);
        }catch (AuthenticationAPIException e){
            HttpHeaders errorHeaders = new HttpHeaders();
            if(e.getMessage().equalsIgnoreCase("User is underage")) {
                errorHeaders.add("Location", "http://localhost:5173?error=underage");
            }
            else{
                errorHeaders.add("Location", "http://localhost:5173/validate-age");
                response.addCookie( authenticationService.createCookie("id_token",accessToken, 300));
            }
            return new ResponseEntity<>(errorHeaders, HttpStatus.FOUND);
        }
        String token = googleAuthenticationService.googleSignIn(accessToken, restTemplate);
        response.addCookie(authenticationService.createCookie("access_token",token, 86400));


        String redirectUrl = "http://localhost:5173";

        HttpHeaders headerz = new HttpHeaders();
        headerz.add("Location", redirectUrl);
        return new ResponseEntity<>(headerz, HttpStatus.FOUND);
    }

    @GetMapping("google/validate-age")
    public ResponseEntity<String> validateAge(@RequestParam int year, @RequestParam int month, @RequestParam int day,
                                              @RequestParam String id_token, HttpServletResponse response){
        try {
            googleApi.checkUserBirthday(year, month, day);
            RestTemplate restTemplate = new RestTemplate();
            String token = googleAuthenticationService.googleSignIn(id_token, restTemplate);
            response.addCookie(authenticationService.createCookie("access_token", token, 86400));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "http://localhost:5173");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }catch (AuthenticationAPIException e){
            HttpHeaders errorHeaders = new HttpHeaders();
            errorHeaders.add("Location", "http://localhost:5173?error=underage");
            return new ResponseEntity<>(errorHeaders, HttpStatus.FOUND);
        }
    }
}

