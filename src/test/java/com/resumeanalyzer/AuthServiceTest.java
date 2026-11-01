package com.resumeanalyzer;

import com.resumeanalyzer.dto.auth.AuthResponse;
import com.resumeanalyzer.dto.auth.LoginRequest;
import com.resumeanalyzer.dto.auth.RegisterRequest;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.BadRequestException;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtTokenProvider;
import com.resumeanalyzer.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    @DisplayName("Should successfully register a new user with hashed password and return JWT")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("Sarah Connor", "sarah@example.com", "SecretPass123", "Java Developer");

        when(userRepository.existsByEmail("sarah@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123")).thenReturn("$2a$10$hashedPasswordValue");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setName("Sarah Connor");
        savedUser.setEmail("sarah@example.com");
        savedUser.setPassword("$2a$10$hashedPasswordValue");
        savedUser.setTargetRole("Java Developer");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken(10L, "sarah@example.com")).thenReturn("mock.jwt.token");

        AuthResponse res = authService.register(req);

        assertThat(res).isNotNull();
        assertThat(res.getToken()).isEqualTo("mock.jwt.token");
        assertThat(res.getEmail()).isEqualTo("sarah@example.com");
        verify(passwordEncoder, times(1)).encode("SecretPass123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration if email is already taken")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Duplicate User", "taken@example.com", "Password123", "QA");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate valid credentials and issue JWT token")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("user@example.com", "CorrectPassword");

        User user = new User("John", "user@example.com", "$2a$10$encodedHash");
        user.setId(5L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectPassword", "$2a$10$encodedHash")).thenReturn(true);
        when(tokenProvider.generateToken(5L, "user@example.com")).thenReturn("valid.bearer.token");

        AuthResponse res = authService.login(req);

        assertThat(res).isNotNull();
        assertThat(res.getToken()).isEqualTo("valid.bearer.token");
        assertThat(res.getUserId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLoginInvalidPassword() {
        LoginRequest req = new LoginRequest("user@example.com", "WrongPassword");

        User user = new User("John", "user@example.com", "$2a$10$encodedHash");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$encodedHash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
