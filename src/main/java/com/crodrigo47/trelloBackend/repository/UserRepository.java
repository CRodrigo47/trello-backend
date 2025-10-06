package com.crodrigo47.trelloBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crodrigo47.trelloBackend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    @Query("SELECT new com.crodrigo47.trelloBackend.dto.UserSearchDto(u.id, u.username) " +
           "FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<com.crodrigo47.trelloBackend.dto.UserSearchDto> searchByUsernamePrefix(
        @Param("prefix") String prefix, Pageable pageable);
 }
