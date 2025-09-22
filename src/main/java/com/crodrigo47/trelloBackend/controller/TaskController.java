package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    private final UserService userService;
    private final TaskService taskService;

    public TaskController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
        .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task id " + id + " not found."));
    }
    
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }
    
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }
    
    @PostMapping("/{taskId}/users/{userId}")
    public Task assignUser(@PathVariable Long taskId, @PathVariable Long userId) {
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.UserNotFoundException("User " + userId + " not found."));
        return taskService.assignTaskToUser(taskId, user);
    }

    @DeleteMapping("/{taskId}/users")
    public Task unassignUser(@PathVariable Long taskId) {
        return taskService.unassignTaskFromUser(taskId);
    }

    @GetMapping("/{taskId}/users")
    public User getUserAssigned(@PathVariable Long taskId){
        Task task = taskService.getTaskById(taskId)
            .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task " + taskId + " not found"));
        
        return task.getAssignedTo();
    }

    @GetMapping("/board/{boardId}")
    public List<Task> getTasksByBoard(@PathVariable Long boardId) {
        return taskService.getTasksByBoard(boardId);
    }

    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId) {
        return taskService.getTasksByUser(userId);
    }

    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable Task.Status status) {
        return taskService.getTasksByStatus(status);
    }
}
