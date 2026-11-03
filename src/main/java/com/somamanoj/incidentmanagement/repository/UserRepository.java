package com.somamanoj.incidentmanagement.repository;

import com.somamanoj.incidentmanagement.entity.Role;
import com.somamanoj.incidentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository = database access layer.
 * Spring Data JPA creates the SQL for these method names automatically.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
