package ru.practicum.shareit.user;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<UserDto> mapToUserDto(Iterable<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(mapToUserDto(user));
        }
        return userDtos;
    }

    public static User mapToUserEntity(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public static List<User> mapToUserEntity(Iterable<UserDto> userDtos) {
        List<User> users = new ArrayList<>();
        for (UserDto user : userDtos) {
            users.add(mapToUserEntity(user));
        }
        return users;
    }
}
