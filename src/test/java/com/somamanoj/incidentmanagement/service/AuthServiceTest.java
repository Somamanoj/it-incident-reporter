package com.somamanoj.incidentmanagement.service;

import com.somamanoj.incidentmanagement.config.JwtService;
import com.somamanoj.incidentmanagement.dto.RegisterRequest;
import com.somamanoj.incidentmanagement.entity.Role;
import com.somamanoj.incidentmanagement.entity.User;
import com.somamanoj.incidentmanagement.exception.BusinessException;
import com.somamanoj.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerCreatesEmployeeAccount() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Soma Manoj");
        request.setEmail("soma@company.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("soma@company.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(jwtService.generateToken("soma@company.com", "EMPLOYEE")).thenReturn("token-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });

        var response = authService.register(request);

        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(response.getToken()).isEqualTo("token-value");
        assertThat(response.getEmail()).isEqualTo("soma@company.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Soma Manoj");
        request.setEmail("soma@company.com");
        request.setPassword("Password123");
        when(userRepository.existsByEmail("soma@company.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }
}
