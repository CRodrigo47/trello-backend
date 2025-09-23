package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.TaskDto;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
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
    public List<TaskDto> getAllTasks(){
        return taskService.getAllTasks().stream()
            .map(DtoMapper::toTaskDto)
            .toList();
    }

    @GetMapping("/{id}")
    public TaskDto getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
            .map(DtoMapper::toTaskDto)
            .orElseThrow(() -> new TaskNotFoundException("Task id " + id + " not found."));
    }

    @PostMapping
    public TaskDto createTask(@RequestBody Task task) {
        return DtoMapper.toTaskDto(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return DtoMapper.toTaskDto(taskService.updateTask(task));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }
    
    @PostMapping("/{taskId}/users/{userId}")
    public TaskDto assignUser(@PathVariable Long taskId, @PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found."));
        return DtoMapper.toTaskDto(taskService.assignTaskToUser(taskId, user));
    }
    
    @DeleteMapping("/{taskId}/users")
    public TaskDto unassignUser(@PathVariable Long taskId) {
        return DtoMapper.toTaskDto(taskService.unassignTaskFromUser(taskId));
    }
    
    @GetMapping("/{taskId}/users")
    public UserDto getUserAssigned(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task " + taskId + " not found"));
        return DtoMapper.toUserDto(task.getAssignedTo());
    }
    
    @GetMapping("/board/{boardId}")
    public List<TaskDto> getTasksByBoard(@PathVariable Long boardId) {
        return taskService.getTasksByBoard(boardId)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }
    
    @GetMapping("/user/{userId}")
    public List<TaskDto> getTasksByUser(@PathVariable Long userId) {
        return taskService.getTasksByUser(userId)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }
    
    @GetMapping("/status/{status}")
    public List<TaskDto> getTasksByStatus(@PathVariable Task.Status status) {
        return taskService.getTasksByStatus(status)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

}
