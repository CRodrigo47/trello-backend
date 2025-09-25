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
    void createEntity_generatedIdValue(){
        User creator = userRepository.save(Builders.buildUser("alice"));
        Board board = boardRepository.save(Builders.buildBoard("Diseño", creator));
    
        assertThat(board.getId()).isNotNull();
        assertThat(board.getCreatedBy()).isNotNull(); // nuevo
        assertThat(board.getCreatedBy().getUsername()).isEqualTo("alice");
    }
    
    
    @Test
    void findByName_returnsMatchingBoards() {
        User creator = userRepository.save(Builders.buildUser("alice"));
    
        boardRepository.save(Builders.buildBoard("Diseño", creator));
        boardRepository.save(Builders.buildBoard("Diseño", creator));
        boardRepository.save(Builders.buildBoard("Programación", creator));
    
        List<Board> result = boardRepository.findByName("Diseño");
        List<Board> resultEmpty = boardRepository.findByName("Marketing");
    
        assertThat(resultEmpty).isEmpty();
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Board::getName)
            .containsOnly("Diseño");
    
        assertThat(result)
            .extracting(Board::getCreatedBy)
            .allMatch(u -> u.getUsername().equals("alice")); // nuevo
    }
    
    
    
    @Transactional
    @Test
    void lazy_or_eager_loading(){
        User creator = userRepository.save(Builders.buildUser("alice"));
        Board board = boardRepository.save(Builders.buildBoard("Diseño", creator));
        User user = userRepository.save(Builders.buildUser("bob"));
    
        taskRepository.save(Builders.buildTask("Tarea1", board, user));
    
        Board loaded = boardRepository.findById(board.getId()).get();
    
        assertThat(loaded.getTasks()).isEmpty(); // depende de fetch type
        assertThat(loaded.getCreatedBy()).isNotNull(); // nuevo
        assertThat(loaded.getCreatedBy().getUsername()).isEqualTo("alice");
    
        boardRepository.save(board);
    
        assertThat(loaded.getTasks()).isEmpty();
    
        Task task = taskRepository.save(Builders.buildTask("Tarea2", board, user));
    
        board.addTask(task);
    
        assertThat(loaded.getTasks()).isNotEmpty(); //No es necesario guardar al añadir
    }

}
