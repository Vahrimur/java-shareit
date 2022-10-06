package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody @Valid ItemRequestDto itemRequestDto)
            throws IncorrectFieldException {
        log.info("Creating request {}, userId={}", itemRequestDto, userId);
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        log.info("Get requests by requester id={}", requesterId);
        return itemRequestClient.getAllItemRequestsByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                         @RequestParam(required = false) Integer from,
                                         @RequestParam(required = false) Integer size) {
        log.info("Get  requests by requester id={}, from {} size {}", requesterId, from, size);
        return itemRequestClient.getAllItemRequests(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(value = "X-Sharer-User-Id") Long requesterId,
                                          @PathVariable("requestId") Long requestId) {
        log.info("Get request id=" + requestId);
        return itemRequestClient.getItemRequestById(requesterId, requestId);
    }
}
