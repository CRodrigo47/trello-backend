package com.crodrigo47.trelloBackend.service;

import java.util.List;
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

    public BoardService(BoardRepository boardRepository, UserRepository userRepository, TaskRepository taskRepository){
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public List<Board> getAllBoardsForCurrentUser(User currentUser) {
        return boardRepository.findByUsersId(currentUser.getId());
    }

    public Board getBoardById(Long id, User currentUser) {
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        
        if (!board.getUsers().contains(currentUser) && !board.getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("Not authorized to access this board");
        }

        return board;
    }

    public Board createBoard(Board board, User creator){
        board.setCreatedBy(creator);
        board.addUser(creator);
        return boardRepository.save(board);
    }

    public Board updateBoard(Board board, User currentUser){
        Board existing = getBoardById(board.getId(), currentUser);

        if(!existing.getCreatedBy().equals(currentUser)){
            throw new RuntimeException("Only the creator can update this board");
        }

        existing.setName(board.getName());
        existing.setDescription(board.getDescription());
        return boardRepository.save(existing);
    }

    public void deleteBoard(Long boardId, User currentUser){
        Board existing = getBoardById(boardId, currentUser);

        if(!existing.getCreatedBy().equals(currentUser)){
            throw new RuntimeException("Only the creator can delete this board");
        }

        boardRepository.delete(existing);
    }

    @Transactional
    public Board addTaskToBoard(Long boardId, Task task, User currentUser) {
        Board board = getBoardById(boardId, currentUser);

        if (!board.getUsers().contains(currentUser)) {
            throw new RuntimeException("You must be a member to add tasks");
        }

        board.addTask(task);
        return boardRepository.save(board);
    }

    public void removeTaskFromBoard(Long boardId, Long taskId, User currentUser){
        Board board = getBoardById(boardId, currentUser);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task id " + taskId + " not found"));

        if (!board.getTasks().contains(task)) {
            throw new RuntimeException("Task not in this board");
        }

        if (!board.getCreatedBy().equals(currentUser) && !task.getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("Not authorized to remove this task");
        }

        board.removeTask(task);
        boardRepository.save(board);
    }

    public Board addUserToBoard(Long boardId, Long userId, User currentUser) {
        Board board = getBoardById(boardId, currentUser);

        if (!board.getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("Only the creator can add users");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        board.addUser(user);
        return boardRepository.save(board);
    }

    public void removeUserFromBoard(Long boardId, Long userId, User currentUser){
        Board board = getBoardById(boardId, currentUser);

        if (!board.getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("Only the creator can remove users");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        board.removeUser(user);
        boardRepository.save(board);
    }

    public Set<Task> getTasksFromBoard(Long boardId, User currentUser) {
        Board board = getBoardById(boardId, currentUser);
        return board.getTasks();
    }

    public Set<User> getUsersFromBoard(Long boardId, User currentUser) {
        Board board = getBoardById(boardId, currentUser);
        return board.getUsers();
    }

    public List<Board> searchBoardsByName(User currentUser, String name) {
    return boardRepository.findByUsersIdAndNameContainingIgnoreCase(currentUser.getId(), name);
    }

}
