package ru.practicum.shareit.userTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private final User user = new User(null, "user", "user@user.com");

    @Test
    void shouldSaveUser() {
        Assertions.assertEquals(new ArrayList<>(), userRepository.findAll());

        userRepository.save(user);

        User testUser = userRepository.getById(1L);

        Assertions.assertEquals(1, userRepository.findAll().size());
        Assertions.assertEquals(1, testUser.getId());
        Assertions.assertEquals(user.getName(), testUser.getName());
        Assertions.assertEquals(user.getEmail(), testUser.getEmail());
    }
}
