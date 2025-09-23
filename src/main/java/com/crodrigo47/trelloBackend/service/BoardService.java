package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.exception.BoardNotFoundException;
import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.exception.UserNotFoundException;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.BoardRepository;
import com.crodrigo47.trelloBackend.repository.TaskRepository;
import com.crodrigo47.trelloBackend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BoardService {
    
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public BoardService(BoardRepository boardRepository, TaskService taskService, UserRepository userRepository, TaskRepository taskRepository){
        this.boardRepository = boardRepository;
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
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

    @Transactional
    public Board addTaskToBoard(Long boardId, Long taskId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));
    
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task " + taskId + " not found"));
    
        board.addTask(task);
        return boardRepository.save(board);
    }

    public void removeTaskFromBoard(Long boardId, Long taskId){
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));

        Task task = taskService.getTaskById(taskId)
            .orElseThrow(() -> new com.crodrigo47.trelloBackend.exception.TaskNotFoundException("Task id " + taskId + " not found"));

        if (!board.getTasks().contains(task)) {
            throw new RuntimeException("Task not in this board");
        }

        board.removeTask(task);
        boardRepository.save(board);
        taskService.deleteTask(task.getId());
    }

    public Board addUserToBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        board.addUser(user);
        return boardRepository.save(board);
    }

    public void removeUserFromBoard(Long boardId, Long userId){
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        board.removeUser(user);
        boardRepository.save(board);
    }

    public Set<Task> getTasksFromBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));

        return board.getTasks();
    }

    public Set<User> getUsersFromBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException("Board " + boardId + " not found"));

        return board.getUsers();
    }
}

