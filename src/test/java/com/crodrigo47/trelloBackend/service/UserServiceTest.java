package com.crodrigo47.trelloBackend.service;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import com.crodrigo47.trelloBackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

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
    void existsByUsername_returnsTrueOrFalse() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        boolean exists = userService.existsByUsername("alice");

        assertThat(exists).isTrue();
        verify(userRepository).existsByUsername("alice");
    }

    @Test
    void getUserByUsername_returnsOptional() {
        User user = Builders.buildUserWithId("bob", 1L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        var result = userService.getUserByUsername("bob");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("bob");
        verify(userRepository).findByUsername("bob");
    }

    @Test
    void searchUsersByUsername_returnsList() {
        when(userRepository.findByUsernameContainingIgnoreCase("bo"))
                .thenReturn(List.of(Builders.buildUser("bob")));

        var result = userService.searchUsersByUsername("bo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("bob");
        verify(userRepository).findByUsernameContainingIgnoreCase("bo");
    }

    @Test
    void updateUser_encodesPasswordAndSaves() {
        User userToUpdate = Builders.buildUserWithId("bob", 1L);
        userToUpdate.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(userToUpdate);

        assertThat(updated.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void deleteUser_callsRepository() {
        userService.deleteUser(10L);

        verify(userRepository).deleteById(10L);
    }
}
