package com.crodrigo47.trelloBackend.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crodrigo47.trelloBackend.exception.TaskNotFoundException;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.BoardRepository;
import com.crodrigo47.trelloBackend.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;

    public TaskService(TaskRepository taskRepository, BoardRepository boardRepository){
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
    }

    private boolean isCreatorOnBoard(Board board, User user) {
        if (board == null || board.getCreatedBy() == null || user == null) return false;
        var creator = board.getCreatedBy();
        if (creator.getId() != null && user.getId() != null) {
            return Objects.equals(creator.getId(), user.getId());
        }
        if (creator == user) return true;
        return creator.getUsername() != null && creator.getUsername().equals(user.getUsername());
    }

    private boolean isMemberOfBoard(Board board, User user) {
        if (board == null || board.getUsers() == null || user == null) return false;

        if (user.getId() != null) {
            return board.getUsers().stream()
                    .anyMatch(u -> u != null && u.getId() != null && Objects.equals(u.getId(), user.getId()));
        }

        return board.getUsers().stream()
                .anyMatch(u -> u == user || (u.getUsername() != null && u.getUsername().equals(user.getUsername())));
    }

    /**
     * A helper that ensures we have a fully loaded Board instance:
     * - if provided Board already has createdBy or users, use it
     * - else, if it has id, load from repository
     * - else return null
     */
    private Board resolveBoardFromTask(Task task) {
        if (task == null || task.getBoard() == null) return null;
        Board b = task.getBoard();

        // if board already has useful info, return it (keeps compatibility with unit tests)
        if ((b.getCreatedBy() != null) || (b.getUsers() != null && !b.getUsers().isEmpty())) {
            return b;
        }

        // otherwise try to load it from repository using id
        if (b.getId() != null) {
            Optional<Board> opt = boardRepository.findById(b.getId());
            return opt.orElse(null);
        }

        return null;
    }

    public Task getTaskById(Long id, User currentUser){
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Board board = task.getBoard();
        if (!isMemberOfBoard(board, currentUser) && !isCreatorOnBoard(board, currentUser)) {
            throw new RuntimeException("Not authorized to access this task");
        }

        return task;
    }

    public Task createTask(Task task, User currentUser){
        // resolver board (puede venir solo con id desde el JSON)
        Board board = resolveBoardFromTask(task);
        if (board == null) {
            throw new RuntimeException("Board reference is required to create a task");
        }

        // reemplazar el board en la tarea por la entidad resuelta (con createdBy/users)
        task.setBoard(board);

        // comprobar membresía o que sea creador
        boolean member = isMemberOfBoard(board, currentUser);
        boolean creator = isCreatorOnBoard(board, currentUser);

        if (!member && !creator) {
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

        // comprobar que el assignee es miembro del board (mismo criterio tolerante)
        Board board = task.getBoard();
        boolean assigneeIsMember = false;
        if (assignee.getId() != null) {
            assigneeIsMember = board.getUsers().stream()
                    .anyMatch(u -> u != null && u.getId() != null && Objects.equals(u.getId(), assignee.getId()));
        } else {
            assigneeIsMember = board.getUsers().stream()
                    .anyMatch(u -> u == assignee || (u.getUsername() != null && u.getUsername().equals(assignee.getUsername())));
        }

        if (!assigneeIsMember) {
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
