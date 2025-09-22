package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.TaskRepository;

@Service
public class TaskService {
    
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id){
        return taskRepository.findById(id);
    }

    public Task createTask(Task task){
        task.setCreatedAt(java.time.LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(Task task){
        task.setUpdatedAt(java.time.LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id){
        taskRepository.deleteById(id);
    }

    public Task assignTaskToUser(Long id, User user){
        Task task = taskRepository.findById(id)
        .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task " + id + " not found."));

        task.assignUser(user);
        return taskRepository.save(task);
    }

    public Task unassignTaskFromUser(Long id){
        Task task = taskRepository.findById(id)
        .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task " + id + " not found."));

        task.unassignUser();
        
        return taskRepository.save(task);
    }

    public List<Task> getTasksByBoard(Long boardId) {
        return taskRepository.findByBoardId(boardId);
    }

    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        return taskRepository.findByStatus(status);
    }
}
