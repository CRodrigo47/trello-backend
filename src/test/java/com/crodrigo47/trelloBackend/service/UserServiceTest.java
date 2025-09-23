package com.crodrigo47.trelloBackend.service;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(Builders.buildUser("bob")));

        var result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(Builders.buildUserWithId("bob", 1L)));

        var result = userService.getUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("bob");
    }

    @Test
    void createUser_returnUser() {
        when(userRepository.save(any(User.class))).thenReturn(Builders.buildUserWithId("alice", 2L));

        var result = userService.createUser(Builders.buildUser("alice"));

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void updateUser_returnUser() {
        User userToUpdate = Builders.buildUserWithId("bob", 1L);
        when(userRepository.save(any(User.class))).thenReturn(userToUpdate);

        var result = userService.updateUser(userToUpdate);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("bob");
    }

    @Test
    void deleteUser_callsRepository() {
        userService.deleteUser(10L);

        verify(userRepository).deleteById(10L);
    }
}
