package com.crodrigo47.trelloBackend.repository;

import com.crodrigo47.trelloBackend.helper.Builders;
import com.crodrigo47.trelloBackend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsUser() {
        // Guardamos un usuario usando el builder
        User saved = userRepository.save(Builders.buildUser("alice"));

        // Buscamos por username
        var found = userRepository.findByUsername("alice");

        // Comprobamos que se ha encontrado y que el ID coincide
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void usernameColumnUnique_throwsException() {
        // Guardamos un usuario
        userRepository.save(Builders.buildUser("bob"));

        // Intentamos guardar otro usuario con el mismo username y comprobamos que falla
        assertThatThrownBy(() -> userRepository.saveAndFlush(Builders.buildUser("bob")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsByUsername_returnsTrueOrFalse() {
        userRepository.save(Builders.buildUser("charlie"));

        assertThat(userRepository.existsByUsername("charlie")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void findByUsernameContainingIgnoreCase_returnsMatchingUsers() {
        userRepository.save(Builders.buildUser("dave"));
        userRepository.save(Builders.buildUser("David"));
        userRepository.save(Builders.buildUser("eve"));

        var results = userRepository.findByUsernameContainingIgnoreCase("dav");
        assertThat(results).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("dave", "David");
    }
}
