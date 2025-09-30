package com.crodrigo47.trelloBackend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createEntity_generatedIdValue() {
        User user = userRepository.save(Builders.buildUser("paul"));
        Board board = Builders.buildBoard("Diseño", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        Task task = Builders.buildTask("tarea", board, user, user);
        taskRepository.save(task);

        assertThat(board.getId()).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(task.getId()).isNotNull();
    }

    @Test
    void saveTask_withBoardAndUser_persistsCorrectly() {
        User user = userRepository.save(Builders.buildUser("bob"));
        Board board = Builders.buildBoard("Diseño", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        Task task = Builders.buildTask("Tarea 1", board, user, user);
        taskRepository.save(task);

        assertThat(task.getId()).isNotNull();
        assertThat(task.getBoard().getId()).isEqualTo(board.getId());
        assertThat(task.getAssignedTo().getId()).isEqualTo(user.getId());
    }

    @Test
    void getAllTasks_returnsList() {
        User user = userRepository.save(Builders.buildUser("alice"));
        Board board = Builders.buildBoard("Programación", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        taskRepository.save(Builders.buildTask("tarea1", board, user, user));
        taskRepository.save(Builders.buildTask("tarea2", board, user, user));

        List<Task> result = taskRepository.findAll();

        assertThat(result).hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void saveTask_withoutBoard_constraintError() {
        Task task = new Task();
        task.setTitle("Tarea persistida");
        task.setBoard(null);
        task.setAssignedTo(Builders.buildUser("bob"));

        assertThatThrownBy(() -> taskRepository.saveAndFlush(task))
                .isInstanceOf(Exception.class);
    }

    @Test
    void findByBoardId_returnsList() {
        User user = userRepository.save(Builders.buildUser("alice"));
        Board board = Builders.buildBoard("Programación", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        taskRepository.save(Builders.buildTask("tarea1", board, user, user));
        taskRepository.save(Builders.buildTask("tarea2", board, user, user));

        List<Task> result = taskRepository.findByBoardIdAndBoardUsersId(board.getId(), user.getId());

        assertThat(result).hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void findByAssignedToId_returnsList() {
        User user = userRepository.save(Builders.buildUser("alice"));
        Board board = Builders.buildBoard("Programación", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        taskRepository.save(Builders.buildTask("tarea1", board, user, user));
        taskRepository.save(Builders.buildTask("tarea2", board, user, user));

        List<Task> result = taskRepository.findByAssignedToIdAndBoardUsersId(user.getId(), user.getId());

        assertThat(result).hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void findByStatus_returnsList() {
        User user = userRepository.save(Builders.buildUser("alice"));
        Board board = Builders.buildBoard("Programación", user);
        board.getUsers().add(user);
        boardRepository.save(board);
    
        taskRepository.save(Builders.buildTaskWithStatus("tarea1", board, user, user, Task.Status.DONE));
        taskRepository.save(Builders.buildTaskWithStatus("tarea2", board, user, user, Task.Status.FUTURE));
        taskRepository.save(Builders.buildTaskWithStatus("tarea3", board, user, user, Task.Status.FUTURE));
    
        // 🔹 Nueva firma: necesitamos boardId y userId
        List<Task> doneTasks = taskRepository.findByBoardIdAndStatusAndBoardUsersId(board.getId(), Task.Status.DONE, user.getId());
        List<Task> futureTasks = taskRepository.findByBoardIdAndStatusAndBoardUsersId(board.getId(), Task.Status.FUTURE, user.getId());
    
        assertThat(doneTasks).hasSize(1)
                .extracting(Task::getTitle)
                .containsExactly("tarea1");
    
        assertThat(futureTasks).hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("tarea2", "tarea3");
    }

    @Test
    void deleteCascade_boardDeletionRemovesTasks() {
        User user = userRepository.save(Builders.buildUser("alice"));
        Board board = Builders.buildBoard("Programación", user);
        board.getUsers().add(user);
        boardRepository.save(board);

        Task task1 = taskRepository.save(Builders.buildTask("tarea1", board, user, user));
        Task task2 = taskRepository.save(Builders.buildTask("tarea2", board, user, user));

        board.addTask(task1);
        board.addTask(task2);
        boardRepository.save(board);

        boardRepository.delete(board);

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void saveOnNonExistentBoard_throwsException() {
        Task task = new Task();
        task.setTitle("Tarea");

        Board fakeBoard = new Board();
        fakeBoard.setId(999L);
        task.setBoard(fakeBoard);
        task.setAssignedTo(userRepository.save(Builders.buildUser("bob")));

        assertThatThrownBy(() -> taskRepository.saveAndFlush(task))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
