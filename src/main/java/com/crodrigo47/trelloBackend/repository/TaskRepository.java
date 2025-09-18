package com.crodrigo47.trelloBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> { }
