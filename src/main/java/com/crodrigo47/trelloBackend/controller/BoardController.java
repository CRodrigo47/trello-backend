package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;
import com.crodrigo47.trelloBackend.service.TaskService;
import com.crodrigo47.trelloBackend.service.UserService;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;





@RestController
@RequestMapping("/boards")
public class BoardController {
    
    private final BoardService boardService;
    private final UserService userService;
    private final TaskService taskService;

    public BoardController(BoardService boardService, UserService userService, TaskService taskService) {
        this.boardService = boardService;
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.getAllBoards();
    }

    @GetMapping("/{id}")
    public Board getBoardById(@PathVariable Long id) {
        return boardService.getBoardById(id)
        .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.BoardNotFoundException("Board id " + id + " not found."));
    }
    
    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardService.createBoard(board);
    }

    @PutMapping("/{id}")
    public Board updateBoard(@PathVariable Long id, @RequestBody Board board) {
        board.setId(id);
        return boardService.updateBoard(board);
    }
    
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id){
        boardService.deleteBoard(id);
    }

    @PostMapping("/{boardId}/users/{userId}")
    public Board addUserToBoard(@PathVariable Long boardId, @PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.UserNotFoundException("User id " + userId + " not found"));
        return boardService.addUserToBoard(boardId, user);
    }

    @DeleteMapping("/{boardId}/users/{userId}")
    public void removeUserFromBoard(@PathVariable Long boardId, @PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.UserNotFoundException("User id " + userId + " not found"));
        boardService.removeUserFromBoard(boardId, user);
    }

    @PostMapping("/{boardId}/tasks")
    public Board addTaskToBoard(@PathVariable Long boardId, @RequestBody Task task) {
        return boardService.addTaskToBoard(boardId, task);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public void removeTaskFromBoard(@PathVariable Long boardId, @PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task id " + taskId + " not found"));
        boardService.removeTaskFromBoard(boardId, task);
    }

    @GetMapping("/{boardId}/tasks")
    public List<Task> getTasksFromBoard(@PathVariable Long boardId) {
        return taskService.getTasksByBoard(boardId);
    }

    @GetMapping("/{boardId}/users")
    public List<User> getUsersFromBoard(@PathVariable Long boardId) {
        Board board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.BoardNotFoundException("Board id " + boardId + " not found"));
        return List.copyOf(board.getUsers());
    }
    
}
