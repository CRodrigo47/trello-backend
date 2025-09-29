package com.crodrigo47.trelloBackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public Task getTaskById(Long id, User currentUser){
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getBoard().getUsers().contains(currentUser) 
                && !task.getBoard().getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("Not authorized to access this task");
        }

        return task;
    }

    public Task createTask(Task task, User currentUser){
        if (!task.getBoard().getUsers().contains(currentUser)) {
            throw new RuntimeException("You must be a member of the board to add tasks");
        }
        task.setCreatedBy(currentUser);
        return taskRepository.save(task);
    }

    public Task updateTask(Task task, User currentUser){
        Task existing = getTaskById(task.getId(), currentUser);
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        return taskRepository.save(existing);
    }

    public void deleteTask(Long id, User currentUser){
        Task task = getTaskById(id, currentUser);
        taskRepository.delete(task);
    }

    public Task assignTaskToUser(Long taskId, User currentUser, User assignee){
        Task task = getTaskById(taskId, currentUser);

        if (!task.getBoard().getUsers().contains(assignee)) {
            throw new RuntimeException("The user to assign must be a member of the board");
        }

        task.assignUser(assignee);
        return taskRepository.save(task);
    }

    public Task unassignTaskFromUser(Long taskId, User currentUser){
        Task task = getTaskById(taskId, currentUser);
        task.unassignUser();
        return taskRepository.save(task);
    }

    public List<Task> getTasksByBoard(Long boardId, Long userId) {
        return taskRepository.findByBoardIdAndBoardUsersId(boardId, userId);
    }

    public List<Task> getTasksByUser(Long userId, Long memberId) {
        return taskRepository.findByAssignedToIdAndBoardUsersId(userId, memberId);
    }

    public List<Task> getTasksByStatus(Long boardId, Task.Status status, Long userId) {
        return taskRepository.findByBoardIdAndStatusAndBoardUsersId(boardId, status, userId);
    }
}
