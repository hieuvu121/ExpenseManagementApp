package com.be9expensphie.expensphie_backend.repositoryTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_whenEmailExists_returnsUser() {
        saveUser("Alice", "alice@example.com", "alice-pass", "token-alice", true);

        var userOptional = userRepository.findByEmail("alice@example.com");

        assertTrue(userOptional.isPresent());
        assertEquals("alice@example.com", userOptional.get().getEmail());
        assertEquals("Alice", userOptional.get().getFullName());
        assertEquals("alice-pass", userOptional.get().getPassword());
    }

    @Test
    void findByEmail_whenEmailDoesNotExist_returnsEmpty() {
        saveUser("Alice", "alice@example.com", "alice-pass", "token-alice", true);

        var userOptional = userRepository.findByEmail("missing@example.com");

        assertTrue(userOptional.isEmpty());
    }

    @Test
    void findByActivationToken_whenTokenExists_returnsUser() {
        saveUser("Bob", "bob@example.com", "bob-pass", "token-bob", false);

        var userOptional = userRepository.findByActivationToken("token-bob");

        assertTrue(userOptional.isPresent());
        assertEquals("bob@example.com", userOptional.get().getEmail());
        assertEquals("token-bob", userOptional.get().getActivationToken());
        assertFalse(userOptional.get().getIsActive());
    }

    @Test
    void findByActivationToken_whenTokenDoesNotExist_returnsEmpty() {
        saveUser("Bob", "bob@example.com", "bob-pass", "token-bob", false);

        var userOptional = userRepository.findByActivationToken("unknown-token");

        assertTrue(userOptional.isEmpty());
    }

    @Test
    void updatePassword_whenEmailExists_updatesOnlyMatchingUser() {
        saveUser("Alice", "alice@example.com", "alice-pass", "token-alice", true);
        saveUser("Bob", "bob@example.com", "bob-pass", "token-bob", true);

        userRepository.updatePassword("alice@example.com", "new-alice-pass");
        entityManager.flush();
        entityManager.clear();

        var updatedAlice = userRepository.findByEmail("alice@example.com").orElseThrow();
        var unchangedBob = userRepository.findByEmail("bob@example.com").orElseThrow();

        assertEquals("new-alice-pass", updatedAlice.getPassword());
        assertEquals("bob-pass", unchangedBob.getPassword());
    }

    @Test
    void updatePassword_whenEmailDoesNotExist_doesNotChangeUsers() {
        saveUser("Alice", "alice@example.com", "alice-pass", "token-alice", true);

        userRepository.updatePassword("missing@example.com", "new-pass");
        entityManager.flush();
        entityManager.clear();

        var alice = userRepository.findByEmail("alice@example.com").orElseThrow();

        assertEquals("alice-pass", alice.getPassword());
    }

    private void saveUser(String fullName, String email, String password, String activationToken, boolean isActive) {
        var user = UserEntity.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .role("ROLE_USER")
                .isActive(isActive)
                .activationToken(activationToken)
                .build();

        entityManager.persistAndFlush(user);
    }
}
