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
        User saved = userRepository.save(User.builder().username("alice").email("alice@mail.com").build());

        var found = userRepository.findByUsername("alice");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void usernameColumnUnique_returnException() {
        userRepository.save(Builders.buildUser("bob"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(Builders.buildUser("bob")))
        .isInstanceOf(DataIntegrityViolationException.class);

    }
}
