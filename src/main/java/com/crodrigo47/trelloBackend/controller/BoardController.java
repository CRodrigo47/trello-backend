package com.crodrigo47.trelloBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.service.BoardService;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;





@RestController
@RequestMapping("/board")
public class BoardController {
    
    private final BoardService boardService;

    public BoardController(BoardService boardService){
        this.boardService = boardService;
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
    
}
