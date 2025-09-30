package com.crodrigo47.trelloBackend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.model.Task;
import com.crodrigo47.trelloBackend.model.User;

import jakarta.transaction.Transactional;

@DataJpaTest
class BoardRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createEntity_generatedIdAndCreator() {
        User creator = userRepository.save(Builders.buildUser("alice"));
        Board board = boardRepository.save(Builders.buildBoard("Diseño", creator));

        assertThat(board.getId()).isNotNull();
        assertThat(board.getCreatedBy()).isNotNull();
        assertThat(board.getCreatedBy().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUsersId_returnsBoardsForUser() {
        User alice = userRepository.save(Builders.buildUser("alice"));
        User bob = userRepository.save(Builders.buildUser("bob"));

        Board board1 = boardRepository.save(Builders.buildBoard("Diseño", alice));
        Board board2 = boardRepository.save(Builders.buildBoard("Programación", alice));
        Board board3 = boardRepository.save(Builders.buildBoard("Marketing", bob));

        board1.addUser(alice);
        board2.addUser(alice);
        board3.addUser(bob);

        boardRepository.save(board1);
        boardRepository.save(board2);
        boardRepository.save(board3);

        List<Board> aliceBoards = boardRepository.findByUsersId(alice.getId());
        List<Board> bobBoards = boardRepository.findByUsersId(bob.getId());

        assertThat(aliceBoards).hasSize(2)
                .extracting(Board::getName)
                .containsExactlyInAnyOrder("Diseño", "Programación");

        assertThat(bobBoards).hasSize(1)
                .extracting(Board::getName)
                .containsExactly("Marketing");
    }

    @Test
    void findByUsersIdAndNameContainingIgnoreCase_returnsMatchingBoards() {
        User alice = userRepository.save(Builders.buildUser("alice"));

        Board board1 = boardRepository.save(Builders.buildBoard("Diseño", alice));
        Board board2 = boardRepository.save(Builders.buildBoard("Programación", alice));
        Board board3 = boardRepository.save(Builders.buildBoard("Diseño UX", alice));

        board1.addUser(alice);
        board2.addUser(alice);
        board3.addUser(alice);

        List<Board> result = boardRepository.findByUsersIdAndNameContainingIgnoreCase(alice.getId(), "diseño");

        assertThat(result).hasSize(2)
                .extracting(Board::getName)
                .containsExactlyInAnyOrder("Diseño", "Diseño UX");
    }

    @Transactional
    @Test
    void tasksLazyLoadingAndBoardTasks() {
        User alice = userRepository.save(Builders.buildUser("alice"));
        User bob = userRepository.save(Builders.buildUser("bob"));

        Board board = boardRepository.save(Builders.buildBoard("Diseño", alice));

        Board loaded = boardRepository.findById(board.getId()).get();

        // Dependiendo del fetch type, inicialmente tasks puede estar vacía
        assertThat(loaded.getTasks()).isEmpty();
        assertThat(loaded.getCreatedBy()).isNotNull();
        assertThat(loaded.getCreatedBy().getUsername()).isEqualTo("alice");

        // Añadimos otra tarea y comprobamos que board recoge la tarea sin guardar explícitamente
        Task task2 = taskRepository.save(Builders.buildTask("Tarea2", board, bob, bob));
        board.addTask(task2);

        assertThat(loaded.getTasks()).isNotEmpty();
        assertThat(loaded.getTasks()).extracting(Task::getTitle)
                .contains("Tarea2");
    }
}
