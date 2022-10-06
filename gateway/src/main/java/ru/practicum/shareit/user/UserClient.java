package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createUser(UserDto userDto) throws IncorrectFieldException {
        checkEmailExists(userDto);
        checkCorrectEmail(userDto);
        return post("", userDto);
    }

    public ResponseEntity<Object> updateUser(UserDto userDto, Long userId) throws IncorrectFieldException {
        checkCorrectEmail(userDto);
        return patch("/" + userId, userDto);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId);
    }

    public ResponseEntity<Object> getUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get("/" + userId);
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
