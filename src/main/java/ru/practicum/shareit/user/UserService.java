package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.IncorrectFieldException;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto) throws IncorrectFieldException;

    UserDto updateUser(UserDto userDto, Long userId)
            throws IncorrectFieldException, IncorrectObjectException;

    void deleteUser(Long userId) throws IncorrectObjectException;

    UserDto getUserById(Long userId) throws IncorrectObjectException;

    List<UserDto> getAllUsers();

    void checkUserExist(Long userId) throws IncorrectObjectException;
}
