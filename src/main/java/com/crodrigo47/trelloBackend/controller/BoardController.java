package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.dto.BoardDto;
import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.TaskDto;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.service.BoardService;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardDto> getAllBoards(@AuthenticationPrincipal User currentUser,
                                       @RequestParam(required = false) String name) {
        List<Board> boards;
        if (name != null && !name.isBlank()) {
            boards = boardService.searchBoardsByName(currentUser, name);
        } else {
            boards = boardService.getAllBoardsForCurrentUser(currentUser);
        }
        return boards.stream()
                .map(DtoMapper::toBoardDto)
                .toList();
    }

    @GetMapping("/{id}")
    public BoardDto getBoardById(@PathVariable Long id,
                                 @AuthenticationPrincipal User currentUser) {
        Board board = boardService.getBoardById(id, currentUser);
        return DtoMapper.toBoardDto(board);
    }

    @PostMapping
    public BoardDto createBoard(@RequestBody Board board,
                                @AuthenticationPrincipal User currentUser) {
        Board created = boardService.createBoard(board, currentUser);
        return DtoMapper.toBoardDto(created);
    }

    @PutMapping("/{id}")
    public BoardDto updateBoard(@PathVariable Long id,
                                @RequestBody Board board,
                                @AuthenticationPrincipal User currentUser) {
        board.setId(id);
        Board updated = boardService.updateBoard(board, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id,
                            @AuthenticationPrincipal User currentUser) {
        boardService.deleteBoard(id, currentUser);
    }

    @PostMapping("/{boardId}/users/{userId}")
    public BoardDto addUserToBoard(@PathVariable Long boardId,
                                   @PathVariable Long userId,
                                   @AuthenticationPrincipal User currentUser) {
        Board updated = boardService.addUserToBoard(boardId, userId, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{boardId}/users/{userId}")
    public void removeUserFromBoard(@PathVariable Long boardId,
                                    @PathVariable Long userId,
                                    @AuthenticationPrincipal User currentUser) {
        boardService.removeUserFromBoard(boardId, userId, currentUser);
    }

    @PostMapping("/{boardId}/tasks")
    public BoardDto addTaskToBoard(@PathVariable Long boardId,
                                   @RequestBody Task task,
                                   @AuthenticationPrincipal User currentUser) {
        Board updated = boardService.addTaskToBoard(boardId, task, currentUser);
        return DtoMapper.toBoardDto(updated);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public void removeTaskFromBoard(@PathVariable Long boardId,
                                    @PathVariable Long taskId,
                                    @AuthenticationPrincipal User currentUser) {
        boardService.removeTaskFromBoard(boardId, taskId, currentUser);
    }

    @GetMapping("/{boardId}/tasks")
    public List<TaskDto> getTasksFromBoard(@PathVariable Long boardId,
                                           @AuthenticationPrincipal User currentUser) {
        return boardService.getTasksFromBoard(boardId, currentUser)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

    @GetMapping("/{boardId}/users")
    public List<UserDto> getUsersFromBoard(@PathVariable Long boardId,
                                           @AuthenticationPrincipal User currentUser) {
        return boardService.getUsersFromBoard(boardId, currentUser)
                .stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }
}

