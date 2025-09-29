package com.crodrigo47.trelloBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
 }
