package com.nam.service;

import com.nam.exception.UserException;
import com.nam.model.*;
import com.nam.payload.request.SignupStudentRequest;
import com.nam.payload.request.SignupTeacherRequest;
import com.nam.repository.RoleRepository;
import com.nam.repository.UserRepository;
import com.nam.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public User findUserById(Long userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }

        throw new UserException("User not found with id: " + userId);
    }

    @Override
    public User findUserByEmail(String email) throws UserException {
        Objects.requireNonNull(email, "email should not be null");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User Not Found with email: " + email));
    }


    @Override
    public User findUserProfileByJwt(String jwt) throws UserException {
        String email = jwtProvider.getEmailFromToken(jwt);
        return findUserByEmail(email);
    }

    @Override
    public void deleteUser(Long id) throws UserException {
        userRepository.deleteById(id);
    }

    @Override
    public Student createStudent(SignupStudentRequest studentRequest) throws UserException {
        userRepository.findByEmail(studentRequest.getEmail())
                .ifPresent(user -> {
                    throw new UserException("User already exists with email: %s".formatted(studentRequest.getEmail()));
                });
        //Immutable
        Set<Role> roles = Set.of(roleRepository.findByName(ERole.ROLE_STUDENT).get());
        //Mutable
/*        Set<Role> roles = new HashSet<>();
        Optional<Role> role = roleRepository.findByName(ERole.ROLE_STUDENT);
        roles.add(role.get());*/

        Student student = Student.builder()
                .firstName(studentRequest.getFirstName()).lastName(studentRequest.getLastName())
                .email(studentRequest.getEmail()).password(passwordEncoder.encode(studentRequest.getPassword()))
                .studentId(studentRequest.getStudentId()).studentClass(studentRequest.getStudentClass())
                .roles(roles)
                .build();

        return userRepository.save(student);
    }

    @Override
    public Teacher createTeacher(SignupTeacherRequest teacherRequest) throws UserException {
        userRepository.findByEmail(teacherRequest.getEmail())
                .ifPresent(user -> {
                    throw new UserException("User already exists with email: %s".formatted(teacherRequest.getEmail()));
                });
        Set<Role> roles = Set.of(roleRepository.findByName(ERole.ROLE_TEACHER).get());
/*        Set<Role> roles = new HashSet<>();
        Optional<Role> role = roleRepository.findByName(ERole.ROLE_TEACHER);
        roles.add(role.get());*/

        Teacher teacher = Teacher.builder()
                .firstName(teacherRequest.getFirstName()).lastName(teacherRequest.getLastName())
                .email(teacherRequest.getEmail()).password(passwordEncoder.encode(teacherRequest.getPassword()))
                .roles(roles)
                .build();

        return userRepository.save(teacher);
    }
}