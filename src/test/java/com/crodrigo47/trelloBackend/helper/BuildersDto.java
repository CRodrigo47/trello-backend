package com.crodrigo47.trelloBackend.helper;

import java.util.HashSet;

import com.crodrigo47.trelloBackend.dto.BoardDtoTest;
import com.crodrigo47.trelloBackend.dto.TaskDtoTest;
import com.crodrigo47.trelloBackend.dto.UserDtoTest;

public class BuildersDto {

    public static UserDtoTest buildUserDto(String username) {
        return new UserDtoTest(
            null,
            username,
            username + "@mail.com",
            null,
            new HashSet<>(), // boards
            new HashSet<>()  // tasks
        );
    }

    public static UserDtoTest buildUserDtoWithId(String username, Long id) {
        return new UserDtoTest(
            id,
            username,
            username + "@mail.com",
            null,
            new HashSet<>(), // boards
            new HashSet<>()  // tasks
        );
    }

    public static BoardDtoTest buildBoardDto(String name, Long createdById) {
        return new BoardDtoTest(
            null,
            name,
            "Descripción de " + name,
            new HashSet<>(), // userIds
            new HashSet<>(),  // taskIds
            createdById
        );
    }

    public static BoardDtoTest buildBoardDtoWithId(String name, Long id, Long createdById) {
        return new BoardDtoTest(
            id,
            name,
            "Descripción de " + name,
            new HashSet<>(), // userIds
            new HashSet<>(),  // taskIds
            createdById
        );
    }

    public static TaskDtoTest buildTaskDto(String title) {
        return new TaskDtoTest(
            null,
            title,
            "Descripción de " + title,
            "FUTURE",
            null, // assignedToId
            null, // boardId
            null,
            null
        );
    }

    public static TaskDtoTest buildTaskDtoWithId(String title, Long id) {
        return new TaskDtoTest(
            id,
            title,
            "Descripción de " + title,
            "FUTURE",
            null, // assignedToId
            null, // boardId
            null,
            null
        );
    }
}
