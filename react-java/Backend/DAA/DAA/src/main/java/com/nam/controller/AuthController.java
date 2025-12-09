package com.nam.controller;

import com.nam.exception.TokenRefreshException;
import com.nam.exception.UserException;
import com.nam.model.RefreshToken;
import com.nam.model.User;
import com.nam.payload.request.LoginRequest;
import com.nam.payload.request.SignupStudentRequest;
import com.nam.payload.request.SignupTeacherRequest;
import com.nam.payload.request.TokenRefreshRequest;
import com.nam.payload.response.ApiResponse;
import com.nam.payload.response.AuthResponse;
import com.nam.payload.response.JwtResponse;
import com.nam.security.jwt.JwtProvider;
import com.nam.security.services.RefreshTokenService;
import com.nam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse> createStudent(@RequestBody SignupStudentRequest request) throws UserException {
        User student = userService.createStudent(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(true)
                .message(String.format("Student with email %s created successfully", student.getEmail()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/signup/teacher")
    public ResponseEntity<ApiResponse> createTeacher(@RequestBody SignupTeacherRequest request) throws UserException {
        User teacher = userService.createTeacher(request);
        ApiResponse apiResponse = ApiResponse.builder()
                .status(true)
                .message(String.format("Teacher with email %s created successfully", teacher.getEmail()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);

    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) throws UserException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        String accessToken = jwtProvider.generateToken(authentication);
        User user = userService.findUserByEmail(loginRequest.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        JwtResponse response = new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                user.getId(),
                user.getFirstName(),
                user.getEmail(),
                user.getRoles()
                        .stream().map(a -> a.getName().name())
                        .toList()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        RefreshToken token = refreshTokenService.findByToken(request.getRefreshToken()).orElseThrow(
                () -> new TokenRefreshException(request.getRefreshToken(), "Refresh token does not exist")
        );
        refreshTokenService.verifyExpiration(token);
        String accessToken = jwtProvider.generateTokenByEmail(token.getUser().getEmail());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(accessToken);
        authResponse.setMessage("Here is your new token");
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                // removing refresh token for user if exist
                User user = userService.findUserByEmail(authentication.getName());
                refreshTokenService.deleteByUserId(user.getId());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        ApiResponse apiResponse = ApiResponse.builder()
                .status(true)
                .message("You have been logged out")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

}