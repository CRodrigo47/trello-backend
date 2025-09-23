package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.dto.BoardDto;
import com.crodrigo47.trelloBackend.dto.DtoMapper;
import com.crodrigo47.trelloBackend.dto.TaskDto;
import com.crodrigo47.trelloBackend.dto.UserDto;
import com.crodrigo47.trelloBackend.exception.BoardNotFoundException;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.service.BoardService;

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

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardDto> getAllBoards() {
        return boardService.getAllBoards().stream()
            .map(DtoMapper::toBoardDto)
            .toList();
    }
    
    @GetMapping("/{id}")
    public BoardDto getBoardById(@PathVariable Long id) {
        return boardService.getBoardById(id)
            .map(DtoMapper::toBoardDto)
            .orElseThrow(() -> new BoardNotFoundException("Board id " + id + " not found."));
    }
    
    @PostMapping
    public BoardDto createBoard(@RequestBody Board board) {
        return DtoMapper.toBoardDto(boardService.createBoard(board));
    }
    
    @PutMapping("/{id}")
    public BoardDto updateBoard(@PathVariable Long id, @RequestBody Board board) {
        board.setId(id);
        return DtoMapper.toBoardDto(boardService.updateBoard(board));
    }

    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id){
        boardService.deleteBoard(id);
    }
    
    @PostMapping("/{boardId}/users/{userId}")
    public BoardDto addUserToBoard(@PathVariable Long boardId, @PathVariable Long userId) {
        return DtoMapper.toBoardDto(boardService.addUserToBoard(boardId, userId));
    }
    
    @DeleteMapping("/{boardId}/users/{userId}")
    public void removeUserFromBoard(@PathVariable Long boardId, @PathVariable Long userId) {
        boardService.removeUserFromBoard(boardId, userId);
    }

    @PostMapping("/{boardId}/tasks")
    public BoardDto addTaskToBoard(@PathVariable Long boardId, @RequestBody Task task) {
        return DtoMapper.toBoardDto(boardService.addTaskToBoard(boardId, task.getId()));
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public void removeTaskFromBoard(@PathVariable Long boardId, @PathVariable Long taskId) {
        boardService.removeTaskFromBoard(boardId, taskId);
    }

    @GetMapping("/{boardId}/tasks")
    public List<TaskDto> getTasksFromBoard(@PathVariable Long boardId) {
        return boardService.getTasksFromBoard(boardId)
                .stream()
                .map(DtoMapper::toTaskDto)
                .toList();
    }

    @GetMapping("/{boardId}/users")
    public List<UserDto> getUsersFromBoard(@PathVariable Long boardId) {
        return boardService.getUsersFromBoard(boardId)
                .stream()
                .map(DtoMapper::toUserDto)
                .toList();
    }
}

