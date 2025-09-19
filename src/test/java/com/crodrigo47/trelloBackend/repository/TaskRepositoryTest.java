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
        Board board = boardRepository.save(Builders.buildBoard("Diseño"));
        User user = userRepository.save(Builders.buildUser("paul"));
        Task task = taskRepository.save(Builders.buildTask("tarea", board, user));

        assertThat(board.getId()).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(task.getId()).isNotNull();
    }

    @Test
    void saveTask_withBoardAnduser_persistsCorrectly() {
        Board board = boardRepository.save(Builders.buildBoard("Diseño"));
        User user = userRepository.save(Builders.buildUser("bob"));

        Task task = taskRepository.save(Builders.buildTask("Tarea 1", board, user));

        assertThat(task.getId()).isNotNull();
        assertThat(task.getBoard().getId()).isEqualTo(board.getId());
        assertThat(task.getAssignedTo().getId()).isEqualTo(user.getId());
    }

    @Test
    void getAllBoards_returnsList(){
        Board board = boardRepository.save(Builders.buildBoard("Programación"));
        User user = userRepository.save(Builders.buildUser("alice"));

        taskRepository.save(Builders.buildTask("tarea1", board, user));
        taskRepository.save(Builders.buildTask("tarea2", board, user));

        List<Task> result = taskRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Task::getTitle).containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void saveTask_withoutBoard_constraintError() {
        Task task = new Task();
        task.setTitle("Tarea persistida");
        task.setBoard(null);
        task.setAssignedTo(Builders.buildUser("bob"));

    assertThatThrownBy(() -> {
        taskRepository.saveAndFlush(task); // 👈 IMPORTANTE: forzar flush
    }).isInstanceOf(Exception.class);
    }

    @Test
    void findByBoardId_returnList(){
         Board board = boardRepository.save(Builders.buildBoard("Programación"));
         User user = userRepository.save(Builders.buildUser("alice"));

          taskRepository.save(Builders.buildTask("tarea1", board, user));
          taskRepository.save(Builders.buildTask("tarea2", board, user));

        List<Task> result = taskRepository.findByBoardId(board.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Task::getTitle).containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void findByAssignedToId_returnList(){
          Board board = boardRepository.save(Builders.buildBoard("Programación"));
          User user = userRepository.save(Builders.buildUser("alice"));

          taskRepository.save(Builders.buildTask("tarea1", board, user));
          taskRepository.save(Builders.buildTask("tarea2", board, user));

        List<Task> result = taskRepository.findByAssignedToId(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Task::getTitle).containsExactlyInAnyOrder("tarea1", "tarea2");
    }

    @Test
    void findByAssignedStatus_returnList(){
         Board board = boardRepository.save(Builders.buildBoard("Programación"));
         User user = userRepository.save(Builders.buildUser("alice"));

          taskRepository.save(Builders.buildTaskWithStatus("tarea1", board, user, Task.Status.DONE));
          taskRepository.save(Builders.buildTaskWithStatus("tarea2", board, user, Task.Status.FUTURE));
          taskRepository.save(Builders.buildTaskWithStatus("tarea3", board, user, Task.Status.FUTURE));

        List<Task> result = taskRepository.findByStatus(Task.Status.DONE);

        assertThat(result).hasSize(1);
        assertThat(result)
            .extracting(Task::getTitle).containsExactlyInAnyOrder("tarea1");

             List<Task> result2 = taskRepository.findByStatus(Task.Status.FUTURE);

        assertThat(result2).hasSize(2);
        assertThat(result2)
            .extracting(Task::getTitle).containsExactlyInAnyOrder("tarea2", "tarea3");
    }

    @Test
    void deleteCascade_returnEmpty(){
         Board board = boardRepository.save(Builders.buildBoard("Programación"));
         User user = userRepository.save(Builders.buildUser("alice"));

        Task task1 = taskRepository.save(Builders.buildTask("tarea1", board, user));
        Task task2 = taskRepository.save(Builders.buildTask("tarea2", board, user));

        board.addTask(task1);
        board.addTask(task2);

        boardRepository.save(board);

        boardRepository.delete(board);

        assertThat(taskRepository.findAll()).isEmpty();
    }
    
    @Test
    void saveOnNonExistentBoard_returnException() {
        Task task = new Task();
            task.setTitle("Tarea");

        Board fakeBoard = new Board();
            fakeBoard.setId(999L); // ID que no existe

            task.setBoard(fakeBoard);
            task.setAssignedTo(userRepository.save(Builders.buildUser("bob")));

        assertThatThrownBy(() -> taskRepository.saveAndFlush(task))
            .isInstanceOf(DataIntegrityViolationException.class);

    }

}
