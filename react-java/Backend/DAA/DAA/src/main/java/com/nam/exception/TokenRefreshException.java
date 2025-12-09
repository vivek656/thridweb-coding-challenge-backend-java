package com.nam.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends CustomApplicationException {

    private static final long serialVersionUID = 1L;

    HttpStatus status = HttpStatus.FORBIDDEN;

    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }
}
