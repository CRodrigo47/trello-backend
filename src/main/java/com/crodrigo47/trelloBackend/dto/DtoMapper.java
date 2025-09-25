package com.crodrigo47.trelloBackend.dto;

import java.util.stream.Collectors;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;

public class DtoMapper {

    public static BoardDto toBoardDto(Board board) {
        return new BoardDto(
            board.getId(),
            board.getName(),
            board.getDescription(),
            board.getUsers().stream().map(User::getId).collect(Collectors.toSet()),
            board.getTasks().stream().map(Task::getId).collect(Collectors.toSet()),
            board.getCreatedBy() != null ? board.getCreatedBy().getId() : null
        );
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getToken(),
            user.getBoards().stream().map(Board::getId).collect(Collectors.toSet()),
            user.getTasks().stream().map(Task::getId).collect(Collectors.toSet())
        );
    }

    public static TaskDto toTaskDto(Task task) {
        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            task.getBoard() != null ? task.getBoard().getId() : null,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
