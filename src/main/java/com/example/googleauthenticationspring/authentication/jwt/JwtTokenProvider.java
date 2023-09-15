package com.example.googleauthenticationspring.authentication.jwt;

import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import com.example.googleauthenticationspring.exception.ResourceNotFoundException;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import com.example.googleauthenticationspring.utils.Messages;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    @Value("${app.jwt-refresh-expiration-miliseconds}")
    private long jwtRefreshExpiry;

    private final UserRepository userRepository;


    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(String username, TokenType tokenType) {
        Date currentDate = new Date();
        Date expireDate;

        if(tokenType.equals(TokenType.ACCESS)) {
             expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        }
        else{
            expireDate = new Date(currentDate.getTime() + jwtRefreshExpiry);
        }
        User user = userRepository.findUserByEmailIgnoreCase(username)
                .orElseThrow( () ->new ResourceNotFoundException("User", "Email", username));
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new AuthenticationAPIException(HttpStatus.UNAUTHORIZED, Messages.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationAPIException(HttpStatus.UNAUTHORIZED, Messages.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationAPIException(HttpStatus.UNAUTHORIZED, Messages.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationAPIException(HttpStatus.UNAUTHORIZED, Messages.JWT_CLAIM_EMPTY);
        }
    }
public boolean validateToken(String token, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    try {
        Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parse(token);
        return true;
    } catch (MalformedJwtException e) {
        // Set a custom error response for invalid JWT token
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(Messages.INVALID_JWT_TOKEN);
        response.getWriter().flush();
        return false; // Return false to indicate validation failure
    } catch (ExpiredJwtException e) {
        // Set a custom error response for expired JWT token
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Expired JWT Token\"}");
        response.getWriter().flush();
        return false; // Return false to indicate validation failure
    } catch (UnsupportedJwtException e) {
        // Set a custom error response for unsupported JWT token
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(Messages.UNSUPPORTED_JWT_TOKEN);
        response.getWriter().flush();
        return false; // Return false to indicate validation failure
    } catch (IllegalArgumentException e) {
        // Set a custom error response for empty JWT claims
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(Messages.JWT_CLAIM_EMPTY);
        response.getWriter().flush();
        return false; // Return false to indicate validation failure
    }
}

}
