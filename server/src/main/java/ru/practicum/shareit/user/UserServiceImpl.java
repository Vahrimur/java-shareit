package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectObjectException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.mapToUserEntity(userDto);
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId)
            throws IncorrectObjectException {
        User user = UserMapper.mapToUserEntity(userDto);
        user.setId(userId);
        checkUserExist(userId);
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
                throw new IncorrectObjectException("There is no user with such ID");
            }
        } else {
            throw new IncorrectObjectException("There is no user with such ID");
        }
    }
}
