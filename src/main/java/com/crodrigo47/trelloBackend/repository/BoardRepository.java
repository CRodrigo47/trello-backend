package com.crodrigo47.trelloBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.Board;

public interface BoardRepository extends JpaRepository<Board, Long> { }
