package com.example.googleauthenticationspring.authentication.jwt;

import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import com.example.googleauthenticationspring.exception.ResourceNotFoundException;
import com.example.googleauthenticationspring.authentication.refresh.RefreshToken;
import com.example.googleauthenticationspring.authentication.refresh.RefreshTokenRepository;
import com.example.googleauthenticationspring.user.User;
import com.example.googleauthenticationspring.user.UserRepository;
import com.example.googleauthenticationspring.utils.Messages;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenProvider(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateToken(String username) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
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
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST, Messages.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST, Messages.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST, Messages.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST, Messages.JWT_CLAIM_EMPTY);
        }
    }
}
