package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.IncorrectUserException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto) throws ValidationException, SameEmailException;
    UserDto updateUser(UserDto userDto, Long userId) throws ValidationException, SameEmailException, IncorrectUserException;
    void deleteUser(Long userId) throws IncorrectUserException;
    UserDto getUserById(Long userId) throws IncorrectUserException;
    List<UserDto> getAllUsers();
    void checkUserExist(Long userId) throws IncorrectUserException;
}
