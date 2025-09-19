package com.crodrigo47.trelloBackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.Board;
import com.crodrigo47.trelloBackend.repository.BoardRepository;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    
    @Mock
    BoardRepository boardRepository;

    @InjectMocks
    BoardService boardService;

    @Test
    void getAllBoards_returnList() {
        when(boardRepository.findAll()).thenReturn(List.of(Builders.buildBoard("Diseño"), Builders.buildBoard("Programación")));

        var result = boardService.getAllBoards();

        assertThat(result).hasSize(2);
        verify(boardRepository).findAll();
    }

    @Test
    void getBoardById_returnBoard(){
        when(boardRepository.findById(1L)).thenReturn(Optional.of(Builders.buildBoardWithId("Diseño", 1L)));

        var result = boardService.getBoardById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Diseño");
    }

    @Test
    void createBoard_returnBoard(){
        Board boardToSave = Builders.buildBoard("Diseño");


        when(boardRepository.save(any(Board.class))).thenReturn(Builders.buildBoardWithId("Diseño", 1L));

        var result = boardService.createBoard(boardToSave);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Diseño");
    }

    @Test
    void updateBoard_returnBoard(){
        Board boardToSave = Builders.buildBoardWithId("Programación", 2L);

        when(boardRepository.save(any(Board.class))).thenReturn(boardToSave);

        Board result = boardService.updateBoard(boardToSave);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Programación");
    }

    @Test
    void deleteBoard_callsRepository(){
        boardService.deleteBoard(5L);

        verify(boardRepository).deleteById(5L);
    }

}
