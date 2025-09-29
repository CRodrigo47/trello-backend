package com.crodrigo47.trelloBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.Board;
import java.util.List;


public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUsersId(Long userId);
    List<Board> findByUsersIdAndNameContainingIgnoreCase(Long userId, String name);
 }
