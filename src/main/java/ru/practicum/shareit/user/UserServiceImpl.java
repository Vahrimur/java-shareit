package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.IncorrectFieldException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) throws IncorrectFieldException {
        User user = UserMapper.mapToUserEntity(userDto);
        checkEmailExists(user);
        checkCorrectEmail(user);
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId)
            throws IncorrectFieldException, IncorrectObjectException {
        User user = UserMapper.mapToUserEntity(userDto);
        user.setId(userId);
        checkUserExist(user.getId());
        checkCorrectEmail(user);
        if (user.getEmail() == null) {
            user.setEmail(userRepository.getById(user.getId()).getEmail());
        }
        if (user.getName() == null) {
            user.setName(userRepository.getById(user.getId()).getName());
        }
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) throws IncorrectObjectException {
        checkUserExist(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUserById(Long userId) throws IncorrectObjectException {
        checkUserExist(userId);
        return UserMapper.mapToUserDto(userRepository.getById(userId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.mapToUserDto(users);
    }

    @Override
    public void checkUserExist(Long userId) throws IncorrectObjectException {
        if (!userRepository.findAll().isEmpty()) {
            List<Long> ids = userRepository.findAll()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(userId)) {
                throw new IncorrectObjectException("Введён некорректный id пользователя");
            }
        } else {
            throw new IncorrectObjectException("Введён некорректный id пользователя");
        }
    }

    private void checkEmailExists(User user) throws IncorrectFieldException {
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IncorrectFieldException("Email не может быть пустым");
        }
    }

    private void checkCorrectEmail(User user) throws IncorrectFieldException {
        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            throw new IncorrectFieldException("Email должен содержать @");
        }
    }
}
