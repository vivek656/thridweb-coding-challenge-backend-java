package com.nam.advice;

import com.nam.exception.CustomApplicationException;
import com.nam.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(CustomApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomApplicationException(CustomApplicationException ex, HttpServletRequest request) {
        ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getServletPath())
                .build();
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(apiErrorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) throws AuthenticationException {
        // handled by security exception handler
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                // we do not want to expose application internal info.
                // to display message handle the exception explicitly
                .message("Something went wrong, please try again later")
                .path(request.getServletPath())
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiErrorResponse);
    }

}
