package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import com.crodrigo47.trelloBackend.dto.BoardDto;
import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.TaskDto;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;
import com.crodrigo47.trelloBackend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    public BoardController(BoardService boardService, UserService userService) {
        this.boardService = boardService;
        this.userService = userService;
    }

    @GetMapping
    public List<BoardDto> getAllBoards(Principal principal,
                                       @RequestParam(required = false) String name) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        List<Board> boards = (name != null && !name.isBlank())
                ? boardService.searchBoardsByName(currentUser, name)
                : boardService.getAllBoardsForCurrentUser(currentUser);

        return boards.stream()
                .map(DtoMapper::toBoardDto)
                .toList();
    }

    @GetMapping("/{id}")
    public BoardDto getBoardById(@PathVariable Long id, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        Board board = boardService.getBoardById(id, currentUser);
        return DtoMapper.toBoardDto(board);
    }

    @PostMapping
    public BoardDto createBoard(@RequestBody Board board, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        Board created = boardService.createBoard(board, currentUser);
        return DtoMapper.toBoardDto(created);
    }

    @PutMapping("/{id}")
    public BoardDto updateBoard(@PathVariable Long id,
                                @RequestBody Board board,
                                Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        board.setId(id);
        Board updated = boardService.updateBoard(board, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        boardService.deleteBoard(id, currentUser);
    }

    @PostMapping("/{boardId}/users/{userId}")
    public BoardDto addUserToBoard(@PathVariable Long boardId,
                                   @PathVariable Long userId,
                                   Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        Board updated = boardService.addUserToBoard(boardId, userId, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{boardId}/users/{userId}")
    public void removeUserFromBoard(@PathVariable Long boardId,
                                    @PathVariable Long userId,
                                    Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        boardService.removeUserFromBoard(boardId, userId, currentUser);
    }

    @PostMapping("/{boardId}/tasks")
    public BoardDto addTaskToBoard(@PathVariable Long boardId,
                                   @RequestBody Task task,
                                   Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        Board updated = boardService.addTaskToBoard(boardId, task, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public void removeTaskFromBoard(@PathVariable Long boardId,
                                    @PathVariable Long taskId,
                                    Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        boardService.removeTaskFromBoard(boardId, taskId, currentUser);
    }

    @GetMapping("/{boardId}/tasks")
    public List<TaskDto> getTasksFromBoard(@PathVariable Long boardId, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        return boardService.getTasksFromBoard(boardId, currentUser)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

    @GetMapping("/{boardId}/users")
    public List<UserDto> getUsersFromBoard(@PathVariable Long boardId, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        return boardService.getUsersFromBoard(boardId, currentUser)
                .stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }
}
