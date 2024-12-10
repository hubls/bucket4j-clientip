package com.example.bucket4jclientip.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //rate limit
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "too many request");

    private final HttpStatus httpStatus;
    private final String message;
}