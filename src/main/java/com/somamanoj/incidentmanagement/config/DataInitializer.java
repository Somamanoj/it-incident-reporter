package com.somamanoj.incidentmanagement.config;

import com.somamanoj.incidentmanagement.entity.Role;
import com.somamanoj.incidentmanagement.entity.User;
import com.somamanoj.incidentmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates demo users on first startup so you can log in immediately.
 * Passwords come from environment variables, not from hardcoded production secrets.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.engineer-email}")
    private String engineerEmail;

    @Value("${app.seed.engineer-password}")
    private String engineerPassword;

    @Value("${app.seed.employee-email}")
    private String employeeEmail;

    @Value("${app.seed.employee-password}")
    private String employeePassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing(adminEmail, "System Administrator", adminPassword, Role.ADMIN);
        createIfMissing(engineerEmail, "IT Support Engineer", engineerPassword, Role.SUPPORT_ENGINEER);
        createIfMissing(employeeEmail, "Demo Employee", employeePassword, Role.EMPLOYEE);
    }

    private void createIfMissing(String email, String fullName, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        userRepository.save(user);
    }
}
