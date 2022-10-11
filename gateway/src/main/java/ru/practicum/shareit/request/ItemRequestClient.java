package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }


    public ResponseEntity<Object> createRequest(long userId, ItemRequestDto itemRequestDto)
            throws IncorrectFieldException {
        checkCorrectDescription(itemRequestDto);
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getAllItemRequestsByRequesterId(Long requesterId) {
        return get("", requesterId);
    }

    public ResponseEntity<Object> getAllItemRequests(Long requesterId, Integer from, Integer size) {
        if ((from == null) || (size == null)) {
            return get("/all", requesterId);
        }
        checkPageableParams(from, size);
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", requesterId, parameters);
    }

    public ResponseEntity<Object> getItemRequestById(Long requesterId, Long requestId) {
        return get("/" + requestId, requesterId);
    }

    private void checkCorrectDescription(ItemRequestDto itemRequestDto) throws IncorrectFieldException {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().equals("")) {
            throw new IncorrectFieldException("The description of the item request cannot be empty");
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
}
