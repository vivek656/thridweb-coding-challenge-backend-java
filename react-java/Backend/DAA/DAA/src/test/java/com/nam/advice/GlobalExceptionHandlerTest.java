package com.nam.advice;

import com.nam.exception.UserException;
import com.nam.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.BDDMockito.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeAll
    void beforeAll() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleCustomApplicationException_will_return_API_error_response() {
        // given - a CustomApplicationException being handled
        String path = "/some/path";
        UserException userException = new UserException("Some message");
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        given(httpServletRequest.getServletPath()).willReturn(path);
        // when - handled by ExceptionHandler
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleCustomApplicationException(userException,httpServletRequest);
        ApiErrorResponse apiErrorResponse = response.getBody();

        //then
        assert response.getStatusCode() == userException.getHttpStatus();
        Assertions.assertNotNull(apiErrorResponse);
        assert apiErrorResponse.getMessage().equals("Some message");
        assert apiErrorResponse.getPath().equals(path);
    }

    @Test
    void handleAuthenticationException_will_rethrow_the_error() {
        AuthenticationException exception = new BadCredentialsException("Some message");
        // given AuthenticationException being handled
        // when , then
        Assertions.assertThrows(AuthenticationException.class, () -> globalExceptionHandler.handleAuthenticationException(exception));
    }

    @Test
    void handleException() {
        // given - A exception being handled
        String path = "/some/path";
        Exception exception = new Exception("Some message");
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        given(httpServletRequest.getServletPath()).willReturn(path);

        // when - handled by exception handler
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler.handleException(exception,httpServletRequest);
        ApiErrorResponse apiErrorResponse = response.getBody();

        // then
        assert response.getStatusCode().is5xxServerError();
        Assertions.assertNotNull(apiErrorResponse);
        assert apiErrorResponse.getMessage().equals("Something went wrong, please try again later");
        assert apiErrorResponse.getPath().equals(path);
    }
}