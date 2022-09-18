package ru.practicum.shareit.userTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserServiceIntegrationTest {
    private final EntityManager em;
    private final UserService service;
    private final UserDto userDto = UserMapper.mapToUserDto(new User(null, "user", "user@user.com"));

    @Test
    void shouldCreateUser() throws Exception {
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        service.createUser(userDto);
        userDto.setName("NewName");
        service.updateUser(userDto, 1L);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();

        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo("NewName"));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> users1 = query.getResultList();

        assertThat(users1, is(not(empty())));

        service.deleteUser(1L);

        List<User> users2 = query.getResultList();

        assertThat(users2, is(empty()));
    }

    @Test
    void shouldGetUserById() throws Exception {
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();

        User testUser = UserMapper.mapToUserEntity(service.getUserById(1L));

        assertThat(testUser, equalTo(user));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u", User.class);
        List<User> users = query.getResultList();

        List<User> testUsers = UserMapper.mapToUserEntity(service.getAllUsers());

        assertThat(testUsers, equalTo(users));
    }
}
