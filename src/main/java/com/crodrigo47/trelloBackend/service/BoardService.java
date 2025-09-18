package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.repository.BoardRepository;

@Service
public class BoardService {
    
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository){
        this.boardRepository = boardRepository;
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoardById(Long id){
        return boardRepository.findById(id);
    }

    public Board createBoard(Board board){
        return boardRepository.save(board);
    }

    public Board updateBoard(Board board){
        return boardRepository.save(board);
    }

    public void deleteBoard(Long id){
        boardRepository.deleteById(id);
    }
}
