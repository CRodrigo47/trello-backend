package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Set;
import java.util.Objects;

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

    /**
     * Decide si 'user' es el creador del board.
     * - Si hay id, comparar por id (más fiable).
     * - Si no hay id (tests con builders), comparar por referencia de objeto o por username.
     */
    private boolean isCreator(Board board, User user) {
        if (board == null || board.getCreatedBy() == null || user == null) {
            return false;
        }

        User creator = board.getCreatedBy();

        // Si ambos ids están presentes, compararlos
        if (creator.getId() != null && user.getId() != null) {
            return Objects.equals(creator.getId(), user.getId());
        }

        // Si alguno no tiene id, intentar comparar por referencia (mismo objeto) o por username
        if (creator == user) return true;
        return creator.getUsername() != null && creator.getUsername().equals(user.getUsername());
    }

    /**
     * Decide si 'user' es miembro del board (está en board.getUsers()).
     * - Preferir comparar por id si disponible.
     * - Si no, comparar por referencia o username.
     */
    private boolean isMember(Board board, User user) {
        if (board == null || board.getUsers() == null || user == null) {
            return false;
        }

        // Si user tiene id, buscar por id en la colección
        if (user.getId() != null) {
            return board.getUsers().stream()
                    .anyMatch(u -> u != null && u.getId() != null && Objects.equals(u.getId(), user.getId()));
        }

        // Si user no tiene id, comparar por referencia o por username
        return board.getUsers().stream()
                .anyMatch(u -> u == user || (u.getUsername() != null && u.getUsername().equals(user.getUsername())));
    }

    public List<Board> getAllBoardsForCurrentUser(User currentUser) {
        return boardRepository.findByUsersId(currentUser.getId());
    }

    public Board getBoardById(Long id, User currentUser) {
        Board board = boardRepository.findById(id)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        
        if (!isMember(board, currentUser) && !isCreator(board, currentUser)) {
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

        if(!isCreator(existing, currentUser)){
            throw new RuntimeException("Only the creator can update this board");
        }

        existing.setName(board.getName());
        existing.setDescription(board.getDescription());
        return boardRepository.save(existing);
    }

    public void deleteBoard(Long boardId, User currentUser){
        Board existing = getBoardById(boardId, currentUser);

        if(!isCreator(existing, currentUser)){
            throw new RuntimeException("Only the creator can delete this board");
        }

        boardRepository.delete(existing);
    }

    @Transactional
    public Board addTaskToBoard(Long boardId, Task task, User currentUser) {
        Board board = getBoardById(boardId, currentUser);

        if (!isMember(board, currentUser)) {
            throw new RuntimeException("You must be a member to add tasks");
        }

        // Asegurarse de que la tarea tiene createdBy y board antes de persistir
        task.setCreatedBy(currentUser);
        task.setBoard(board);

        // Añadir y guardar
        board.addTask(task);
        return boardRepository.save(board);
    }

    public void removeTaskFromBoard(Long boardId, Long taskId, User currentUser){
        Board board = getBoardById(boardId, currentUser);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task id " + taskId + " not found"));

        if (board.getTasks().stream().noneMatch(t -> Objects.equals(t.getId(), task.getId()))) {
            throw new RuntimeException("Task not in this board");
        }

        Long taskCreatorId = task.getCreatedBy() != null ? task.getCreatedBy().getId() : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        if (!isCreator(board, currentUser) && !Objects.equals(taskCreatorId, currentUserId)) {
            throw new RuntimeException("Not authorized to remove this task");
        }

        board.removeTask(task);
        boardRepository.save(board);
    }

    public Board addUserToBoard(Long boardId, Long userId, User currentUser) {
        Board board = getBoardById(boardId, currentUser);

        if (!isCreator(board, currentUser)) {
            throw new RuntimeException("Only the creator can add users");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        board.addUser(user);
        return boardRepository.save(board);
    }

    public void removeUserFromBoard(Long boardId, Long userId, User currentUser){
        Board board = getBoardById(boardId, currentUser);

        if (!isCreator(board, currentUser)) {
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
