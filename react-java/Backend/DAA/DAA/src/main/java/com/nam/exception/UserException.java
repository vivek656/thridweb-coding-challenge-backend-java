package com.nam.exception;

import org.springframework.http.HttpStatus;

public class UserException extends CustomApplicationException {

    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public UserException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
