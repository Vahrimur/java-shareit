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
        checkCorrectDescription(itemRequestDto);
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
        if (!(from == null) && !(size == null)) {
            checkPageableParams(from, size);
        }
        log.info("Get  requests by requester id={}, from {} size {}", requesterId, from, size);
        return itemRequestClient.getAllItemRequests(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(value = "X-Sharer-User-Id") Long requesterId,
                                          @PathVariable("requestId") Long requestId) {
        log.info("Get request id=" + requestId);
        return itemRequestClient.getItemRequestById(requesterId, requestId);
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
