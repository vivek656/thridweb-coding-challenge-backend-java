package com.nam.exception;

import org.springframework.http.HttpStatus;

public class CustomApplicationException extends RuntimeException {

    protected HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public CustomApplicationException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
