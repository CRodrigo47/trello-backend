package com.crodrigo47.trelloBackend.helper;

import java.util.HashSet;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.model.User.Role;

public class Builders {

    // ------------------- USER -------------------
    public static User buildUser(String username) {
        return User.builder()
            .username(username)
            .password(username)
            .role(Role.USER)
            .boards(new HashSet<>())
            .tasks(new HashSet<>())
            .build();
    }

    public static User buildUserWithId(String username, Long id) {
        return User.builder()
            .id(id)
            .username(username)
            .password(username)
            .role(Role.USER)
            .boards(new HashSet<>())
            .tasks(new HashSet<>())
            .build();
    }

    // ------------------- BOARD -------------------
    public static Board buildBoard(String name, User createdBy) {
        return Board.builder()
            .name(name)
            .description("Descripción de " + name)
            .createdBy(createdBy)
            .users(new HashSet<>())
            .tasks(new HashSet<>())
            .build();
    }

    public static Board buildBoardWithId(String name, Long id, User createdBy) {
        return Board.builder()
            .id(id)
            .name(name)
            .description("Descripción de " + name)
            .createdBy(createdBy)
            .users(new HashSet<>())
            .tasks(new HashSet<>())
            .build();
    }

    // ------------------- TASK -------------------
    public static Task buildTask(String title, Board board, User createdBy, User assignedTo) {
        return Task.builder()
            .title(title)
            .description("Descripción de " + title)
            .status(Task.Status.FUTURE)
            .board(board)
            .createdBy(createdBy)
            .assignedTo(assignedTo)
            .build();
    }

    public static Task buildTaskWithStatus(String title, Board board, User createdBy, User assignedTo, Task.Status status) {
        return Task.builder()
            .title(title)
            .description("Descripción de " + title)
            .status(status)
            .board(board)
            .createdBy(createdBy)
            .assignedTo(assignedTo)
            .build();
    }

    public static Task buildTaskWithId(String title, Long id, Board board, User createdBy, User assignedTo) {
        return Task.builder()
            .id(id)
            .title(title)
            .description("Descripción de " + title)
            .status(Task.Status.FUTURE)
            .board(board)
            .createdBy(createdBy)
            .assignedTo(assignedTo)
            .build();
    }
}
