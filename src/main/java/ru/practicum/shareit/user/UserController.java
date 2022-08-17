package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.exception.IncorrectFieldException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) throws IncorrectFieldException, SameEmailException {
        userDto = userService.createUser(userDto);
        log.info("POST /users {}", userDto);
        return userDto;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable("userId") Long userId) throws IncorrectFieldException, SameEmailException, IncorrectObjectException {
        userDto = userService.updateUser(userDto, userId);
        log.info("PATCH /users {}", userDto);
        return userDto;
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable("userId") Long userId) throws IncorrectObjectException {
        log.info("DELETE /users/" + userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable("userId") Long userId) throws IncorrectObjectException {
        log.info("GET /users/" + userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("GET /users");
        return userService.getAllUsers();
    }
}
