package com.crodrigo47.trelloBackend.repository;

import com.crodrigo47.trelloBackend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

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
}
