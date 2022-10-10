package ru.practicum.shareit.userTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.user.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private MockitoSession session;
    private final User userCorrect = new User(1L, "user", "user@user.com");
    private final UserDto userCorrectDto = UserMapper.mapToUserDto(userCorrect);
    private final User userNoEmail = new User(2L, "user", "");
    private final UserDto userNoEmailDto = UserMapper.mapToUserDto(userNoEmail);
    private final User userWrongEmail = new User(3L, "user", "email");
    private final UserDto userWrongEmailDto = UserMapper.mapToUserDto(userWrongEmail);

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        userService = new UserServiceImpl(userRepository);
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    void shouldCreateUserCorrect() throws IncorrectFieldException {
        Mockito
                .when(userRepository.save(any()))
                .thenReturn(userCorrect);

        UserDto newUser = userService.createUser(userCorrectDto);

        Assertions.assertEquals(userCorrectDto.getId(), newUser.getId());
        Assertions.assertEquals(userCorrectDto.getName(), newUser.getName());
        Assertions.assertEquals(userCorrectDto.getEmail(), newUser.getEmail());

        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(userCorrect);
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldUpdateUserCorrect() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userCorrect));
        Mockito
                .when(userRepository.save(any()))
                .thenReturn(userCorrect);

        UserDto updatedUser = userService.updateUser(userCorrectDto, 1L);

        Assertions.assertEquals(userCorrectDto.getId(), updatedUser.getId());
        Assertions.assertEquals(userCorrectDto.getName(), updatedUser.getName());
        Assertions.assertEquals(userCorrectDto.getEmail(), updatedUser.getEmail());

        Mockito
                .verify(userRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(userCorrect);
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldUpdateUserCorrectZeroFields() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userCorrect));
        Mockito
                .when(userRepository.save(any()))
                .thenReturn(userCorrect);
        Mockito
                .when(userRepository.getById(1L))
                .thenReturn(userCorrect);
        String email = userCorrectDto.getEmail();
        String name = userCorrectDto.getName();
        userCorrectDto.setEmail(null);
        userCorrectDto.setName(null);
        UserDto updatedUser = userService.updateUser(userCorrectDto, 1L);
        userCorrectDto.setName(name);
        userCorrectDto.setEmail(email);

        Assertions.assertEquals(userCorrectDto.getId(), updatedUser.getId());
        Assertions.assertEquals(userCorrectDto.getName(), updatedUser.getName());
        Assertions.assertEquals(userCorrectDto.getEmail(), updatedUser.getEmail());

        Mockito
                .verify(userRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(userCorrect);
        Mockito
                .verify(userRepository, Mockito.times(2))
                .getById(1L);
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldUpdateUserNoExists() {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(new ArrayList<>());

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> userService.updateUser(userWrongEmailDto, 3L));

        Mockito
                .verify(userRepository, Mockito.times(1))
                .findAll();
        Assertions.assertEquals("There is no user with such ID", exception.getMessage());
    }

    @Test
    void shouldDeleteUserCorrect() throws IncorrectObjectException {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userCorrect));

        userService.deleteUser(1L);

        Mockito
                .verify(userRepository, Mockito.times(1))
                .deleteById(1L);
        Mockito
                .verify(userRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldDeleteUserNoExists() {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userWrongEmail));

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> userService.deleteUser(1L));

        Mockito
                .verify(userRepository, Mockito.times(2))
                .findAll();
        Assertions.assertEquals("There is no user with such ID", exception.getMessage());
    }

    @Test
    void shouldGetById() throws IncorrectObjectException {
        Mockito
                .when(userRepository.getById(1L))
                .thenReturn(userCorrect);
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userCorrect));

        UserDto getUser = userService.getUserById(1L);

        Assertions.assertEquals(userCorrectDto.getId(), getUser.getId());
        Assertions.assertEquals(userCorrectDto.getName(), getUser.getName());
        Assertions.assertEquals(userCorrectDto.getEmail(), getUser.getEmail());

        Mockito
                .verify(userRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verify(userRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldGetAllUsers() {
        Mockito
                .when(userRepository.findAll())
                .thenReturn(List.of(userCorrect, userWrongEmail));

        List<UserDto> users = userService.getAllUsers();

        Assertions.assertEquals(users.size(), 2);
        Assertions.assertEquals(userCorrectDto.getId(), users.get(0).getId());
        Assertions.assertEquals(userCorrectDto.getName(), users.get(0).getName());
        Assertions.assertEquals(userCorrectDto.getEmail(), users.get(0).getEmail());

        Mockito
                .verify(userRepository, Mockito.times(1))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(userRepository);
    }
}
