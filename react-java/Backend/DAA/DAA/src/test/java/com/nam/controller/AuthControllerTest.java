package com.nam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nam.advice.GlobalExceptionHandler;
import com.nam.model.RefreshToken;
import com.nam.model.Student;
import com.nam.model.Teacher;
import com.nam.payload.request.LoginRequest;
import com.nam.payload.request.SignupStudentRequest;
import com.nam.payload.request.SignupTeacherRequest;
import com.nam.payload.request.TokenRefreshRequest;
import com.nam.security.jwt.JwtProvider;
import com.nam.security.jwt.JwtValidator;
import com.nam.security.services.RefreshTokenService;
import com.nam.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final UserService userService = mock(UserService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);


    private final ObjectMapper objectMapper = new ObjectMapper();

    private Student student;
    private Teacher teacher;

    private RefreshToken refreshToken;
    private Authentication authentication;
    private String authToken;

    @BeforeEach
    void beforeAll() {
        AuthController authController = new AuthController(
                jwtProvider, userService, authenticationManager, refreshTokenService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .addFilter(new JwtValidator())
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        refreshToken = new RefreshToken();
        refreshToken.setToken("1234");
        refreshToken.setId(1);
        refreshToken.setUser(student);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        authToken = "my.auth.token";
    }

    @BeforeEach
    void beforeEach() {
        student = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@host.com")
                .roles(Set.of())
                .password("pass")
                .studentClass("5")
                .studentId("34")
                .build();
        teacher = Teacher.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@host.com")
                .roles(Set.of())
                .password("pass")
                .build();
        authentication = mock(Authentication.class);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getName()).willReturn(student.getEmail());
    }

    @AfterEach
    void afterEach() {
        reset(userService,authenticationManager,refreshTokenService,jwtProvider);
    }

    @Test
    void createStudent_will_succeed_for_valid_request() throws Exception {
        //given - a signup student request
        SignupStudentRequest signupStudentRequest = SignupStudentRequest.builder()
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .password(student.getPassword())
                .studentClass(student.getStudentClass())
                .studentId(student.getStudentId())
                .build();

        given(userService.createStudent(any(SignupStudentRequest.class))).willReturn(student);

        // when - we call the signup API controller
        // then - verify response
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupStudentRequest))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(String.format("Student with email %s created successfully", student.getEmail())));

        // then - verify service
        verify(userService, times(1)).createStudent(any(SignupStudentRequest.class));
    }

    @Test
    void createTeacher_will_succeed_for_valid_request() throws Exception {
        //given - a signup student request
        SignupTeacherRequest signupTeacherRequest = SignupTeacherRequest.builder()
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .build();

        given(userService.createTeacher(any(SignupTeacherRequest.class))).willReturn(teacher);

        // when - we call the signup API controller
        // then - verify response
        mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupTeacherRequest))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(String.format("Teacher with email %s created successfully", student.getEmail())));

        // then - verify service
        verify(userService, times(1)).createTeacher(any(SignupTeacherRequest.class));
    }

    @Test
    void authenticateUser_will_succeed_for_valid_auth_request() throws Exception {
        // given - a login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(student.getEmail());
        loginRequest.setPassword(student.getPassword());


        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        given(jwtProvider.generateToken(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authToken);
        given(userService.findUserByEmail(student.getEmail())).willReturn(student);
        given(refreshTokenService.createRefreshToken(student.getId())).willReturn(refreshToken);

        // when - we call signin API
        // then - response contains the accessToken and other user credentials
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.email").value(student.getEmail()))
                .andExpect(jsonPath("$.accessToken").value(authToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken.getToken()));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProvider, times(1)).generateToken(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).findUserByEmail(student.getEmail());
        verify(refreshTokenService, times(1)).createRefreshToken(student.getId());
    }

    @Test
    void refreshtoken_will_succeed_for_valid_refresh_token() throws Exception {
        // given - a request to get token from a refresh token
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(refreshToken.getToken());

        given(refreshTokenService.findByToken(refreshRequest.getRefreshToken())).willReturn(Optional.of(refreshToken));
        given(jwtProvider.generateTokenByEmail(refreshToken.getUser().getEmail())).willReturn(authToken);

        // when - we call refreshtoken API
        // then - API response will contain the new jwt token
        mockMvc.perform(post("/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").value(authToken))
                .andExpect(jsonPath("$.message").value("Here is your new token"));

        verify(jwtProvider, times(1)).generateTokenByEmail(refreshToken.getUser().getEmail());
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
    }

    @Test
    void refreshtoken_fails_when_token_not_found() throws Exception {

        //given - request to get token for a refresh token not in db.
        String tokenNotInDB = "tokenNotInDB";
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(tokenNotInDB);

        given(refreshTokenService.findByToken(refreshRequest.getRefreshToken())).willReturn(Optional.empty());

        // when - we call the refreshtoken API
        // then - we get FORBIDDEN error
        mockMvc.perform(post("/auth/refreshtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest))
                ).andExpect(status().isForbidden());
    }


    @Test
    void logoutUser_succeeds_for_valid_request() throws Exception {

        // given - a request to signout and a logged-in user
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);

        given(userService.findUserByEmail(authentication.getName())).willReturn(student);
        given(refreshTokenService.deleteByUserId(student.getId())).willReturn(1);

        try(securityContextHolderMockedStatic) {
            given(SecurityContextHolder.getContext()).willReturn(securityContext);
            // when - we call signout API
            // then
            mockMvc.perform(post("/auth/signout")
                            .contentType(MediaType.APPLICATION_JSON)
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value("You have been logged out"));

        }

        //then-  refresh token is deleted
        verify(userService, times(1)).findUserByEmail(authentication.getName());
        verify(refreshTokenService, times(1)).deleteByUserId(student.getId());
    }
}