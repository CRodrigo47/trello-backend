package com.crodrigo47.trelloBackend.helper;

import java.util.HashSet;

import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;

public class Builders {

    public static User buildUser(String username){
            return User.builder()
               .username(username)
               .email(username + "@mail.com")
               .build();
    }

    public static User buildUserWithId(String username, Long id){
            return User.builder()
               .username(username)
               .id(id)
               .email(username + "@mail.com")
               .build();
    }

    public static Board buildBoard(String name) {
            return Board.builder()
                .name(name)
                .description("Descripción de " + name)
                .users(new HashSet<>())   // vacío por defecto
                .tasks(new HashSet<>())   // vacío por defecto
                .build();
    }

        public static Board buildBoardWithId(String name, Long id) {
            return Board.builder()
                .name(name)
                .id(id)
                .description("Descripción de " + name)
                .users(new HashSet<>())   // vacío por defecto
                .tasks(new HashSet<>())   // vacío por defecto
                .build();
    }

    public static Task buildTask(String title, Board board, User assignedTo) {
         Task task = new Task();
         task.setTitle(title);
         task.setDescription("Descripción de " + title);
         task.setStatus(Task.Status.FUTURE);
         task.setBoard(board);
         task.setAssignedTo(assignedTo);
         return task;
    }

    public static Task buildTaskWithStatus(String title, Board board, User assignedTo, Task.Status status) {
         Task task = new Task();
         task.setTitle(title);
         task.setDescription("Descripción de " + title);
         task.setStatus(status);
         task.setBoard(board);
         task.setAssignedTo(assignedTo);
         return task;
    }

        public static Task buildTaskWithId(String title, Long id, Board board, User assignedTo) {
         Task task = new Task();
         task.setTitle(title);
         task.setId(id);
         task.setDescription("Descripción de " + title);
         task.setStatus(Task.Status.FUTURE);
         task.setBoard(board);
         task.setAssignedTo(assignedTo);
         return task;
    }
}
