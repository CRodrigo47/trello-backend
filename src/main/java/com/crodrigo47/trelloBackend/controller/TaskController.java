package com.crodrigo47.trelloBackend.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.TaskDto;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User resolveCurrentUser(String username) {
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    @GetMapping("/{id}")
    public TaskDto getTaskById(@PathVariable Long id,
                               @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        Task task = taskService.getTaskById(id, currentUser);
        return DtoMapper.toTaskDto(task);
    }

    @PostMapping
    public TaskDto createTask(@RequestBody Task task,
                              @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        Task created = taskService.createTask(task, currentUser);
        return DtoMapper.toTaskDto(created);
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id,
                              @RequestBody Task task,
                              @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        task.setId(id);
        Task updated = taskService.updateTask(task, currentUser);
        return DtoMapper.toTaskDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id,
                           @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        taskService.deleteTask(id, currentUser);
    }

    @PostMapping("/{taskId}/users/{userId}")
    public TaskDto assignUser(@PathVariable Long taskId,
                              @PathVariable Long userId,
                              @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);

        User assignee = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        Task task = taskService.assignTaskToUser(taskId, currentUser, assignee);
        return DtoMapper.toTaskDto(task);
    }

    @DeleteMapping("/{taskId}/users")
    public TaskDto unassignUser(@PathVariable Long taskId,
                                @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        Task task = taskService.unassignTaskFromUser(taskId, currentUser);
        return DtoMapper.toTaskDto(task);
    }

    @GetMapping("/{taskId}/users")
    public UserDto getUserAssigned(@PathVariable Long taskId,
                                   @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        Task task = taskService.getTaskById(taskId, currentUser);
        User assigned = task.getAssignedTo();
        if (assigned == null) {
            throw new UserNotFoundException("No user assigned to task " + taskId);
        }
        return DtoMapper.toUserDto(assigned);
    }

    @GetMapping("/board/{boardId}")
    public List<TaskDto> getTasksByBoard(@PathVariable Long boardId,
                                         @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        return taskService.getTasksByBoard(boardId, currentUser.getId())
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<TaskDto> getTasksByUser(@PathVariable Long userId,
                                        @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        return taskService.getTasksByUser(userId, currentUser.getId())
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

    @GetMapping("/status/{status}/board/{boardId}")
    public List<TaskDto> getTasksByStatus(@PathVariable Task.Status status,
                                          @PathVariable Long boardId,
                                          @AuthenticationPrincipal String username) {
        User currentUser = resolveCurrentUser(username);
        return taskService.getTasksByStatus(boardId, status, currentUser.getId())
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }
}
