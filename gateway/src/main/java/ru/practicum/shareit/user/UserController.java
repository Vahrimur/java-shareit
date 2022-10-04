package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
	public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) throws IncorrectFieldException {
		checkEmailExists(userDto);
		checkCorrectEmail(userDto);
		log.info("Creating user {}", userDto);
		return userClient.createUser(userDto);
	}

	@PatchMapping("/{userId}")
	public ResponseEntity<Object> update(@RequestBody @Valid UserDto userDto,
										 @PathVariable("userId") Long userId) throws IncorrectFieldException {
		checkCorrectEmail(userDto);
		log.info("Updating user {}", userDto);
		return userClient.updateUser(userDto, userId);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Object> delete(@PathVariable("userId") Long userId) {
		log.info("Delete user {}", userId);
		return userClient.deleteUser(userId);
	}

	@GetMapping
	public ResponseEntity<Object> getUsers() {
		log.info("Get users");
		return userClient.getUsers();
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Object> getUser(@PathVariable("userId") Long userId) {
		log.info("Get user {}", userId);
		return userClient.getUserById(userId);
	}

	private void checkEmailExists(UserDto user) throws IncorrectFieldException {
		if (!StringUtils.hasText(user.getEmail())) {
			throw new IncorrectFieldException("The email of the user cannot be empty");
		}
	}

	private void checkCorrectEmail(UserDto user) throws IncorrectFieldException {
		if (user.getEmail() != null && !user.getEmail().contains("@")) {
			throw new IncorrectFieldException("The email of the user must contain @");
		}
	}
}
