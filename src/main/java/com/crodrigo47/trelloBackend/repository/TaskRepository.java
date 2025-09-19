package com.crodrigo47.trelloBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> { 
    List<Task> findByBoardId(Long boardId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByStatus(Task.Status status);
}
