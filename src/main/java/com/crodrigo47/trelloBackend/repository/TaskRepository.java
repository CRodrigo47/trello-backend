package com.crodrigo47.trelloBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crodrigo47.trelloBackend.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> { 
    List<Task> findByBoardIdAndBoardUsersId(Long boardId, Long userId);
    List<Task> findByAssignedToIdAndBoardUsersId(Long userId, Long memberId);
    List<Task> findByBoardIdAndStatusAndBoardUsersId(Long boardId, Task.Status status, Long userId);

}
