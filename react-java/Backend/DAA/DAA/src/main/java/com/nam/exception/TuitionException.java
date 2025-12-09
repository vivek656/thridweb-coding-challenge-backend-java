package com.nam.exception;

import org.springframework.http.HttpStatus;

public class TuitionException extends CustomApplicationException {

    protected HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public TuitionException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
