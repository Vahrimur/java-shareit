package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.IncorrectUserException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) throws ValidationException, SameEmailException {
        User user = UserMapper.mapToUserEntity(userDto);
        checkEmailExists(user);
        checkCorrectEmail(user);
        return UserMapper.mapToUserDto(userRepository.createUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) throws ValidationException, SameEmailException, IncorrectUserException {
        User user = UserMapper.mapToUserEntity(userDto);
        user.setId(userId);
        checkUserExist(user.getId());
        checkCorrectEmail(user);
        if (user.getEmail() == null) {
            user.setEmail(userRepository.getUserById(user.getId()).getEmail());
        }
        if (user.getName() == null) {
            user.setName(userRepository.getUserById(user.getId()).getName());
        }
        return UserMapper.mapToUserDto(userRepository.updateUser(user));
    }

    @Override
    public void deleteUser(Long userId) throws IncorrectUserException {
        checkUserExist(userId);
        userRepository.deleteUser(userId);
    }

    @Override
    public UserDto getUserById(Long userId) throws IncorrectUserException {
        checkUserExist(userId);
        return UserMapper.mapToUserDto(userRepository.getUserById(userId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toList());
    }

    @Override
    public void checkUserExist(Long userId) throws IncorrectUserException {
        if (!userRepository.getAllUsers().isEmpty()) {
            List<Long> ids = userRepository.getAllUsers().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(userId)) {
                throw new IncorrectUserException("Не существует пользователя с таким id.");
            }
        }  else {
            throw new IncorrectUserException("Не существует пользователя с таким id.");
        }
    }

    private void checkEmailExists(User user) throws ValidationException {
        if (!StringUtils.hasText(user.getEmail())) {
            throw new ValidationException("Email не может быть пустым.");
        }
    }

    private void checkCorrectEmail(User user) throws ValidationException, SameEmailException {
        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать @.");
        }
        if (!userRepository.getAllUsers().isEmpty()) {
            List<String> emails = userRepository.getAllUsers().stream()
                    .map(User::getEmail)
                    .collect(Collectors.toList());
            if (emails.contains(user.getEmail())) {
                throw new SameEmailException("Пользователь с таким email уже существует.");
            }
        }
    }
}
