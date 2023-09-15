package com.example.googleauthenticationspring.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class AccessTokenException extends RuntimeException{
    private HttpStatus status;
    private String message;

    public AccessTokenException(String message, HttpStatus httpStatus){
        this.message = message;
        this.status = httpStatus;
    }
}
