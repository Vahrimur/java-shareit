package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto itemDto) throws IncorrectFieldException {
        checkCorrectItem(itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, ItemDto itemDto, Long itemId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllItemsByUserId(Long userId, Integer from, Integer size) {
        if ((from == null) || (size == null)) {
            return get("", userId);
        }
        checkPageableParams(from, size);
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getAllItemsByByText(String text, Integer from, Integer size, Long userId) {
        if ((from == null) || (size == null)) {
            return get("/search?text=" + text, userId);
        }
        checkPageableParams(from, size);
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId, CommentDto commentDto)
            throws IncorrectFieldException {
        checkTextExists(commentDto);
        return post("/" + itemId + "/comment", userId, commentDto);
    }

    private void checkCorrectItem(ItemDto item) throws IncorrectFieldException {
        if (item.getAvailable() == null || !item.getAvailable()) {
            throw new IncorrectFieldException("The item must be available for booking");
        }
        if (item.getName() == null || item.getName().equals("")) {
            throw new IncorrectFieldException("The name of the item cannot be empty");
        }
        if (item.getDescription() == null) {
            throw new IncorrectFieldException("The description of the item cannot be empty");
        }
    }

    private void checkPageableParams(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Index of start element cannot be less zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal zero");
        }
    }

    private void checkTextExists(CommentDto commentDto) throws IncorrectFieldException {
        if (!StringUtils.hasText(commentDto.getText())) {
            throw new IncorrectFieldException("The comment text cannot be empty");
        }
    }
}
