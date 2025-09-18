package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.model.Task;
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
}
