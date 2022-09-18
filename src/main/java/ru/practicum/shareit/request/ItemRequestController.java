package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = "X-Sharer-User-Id") Long requesterId,
                                 @RequestBody ItemRequestDto itemRequestDto)
            throws IncorrectObjectException, IncorrectFieldException {
        itemRequestDto = itemRequestService.addNewItemRequest(requesterId, itemRequestDto);
        log.info("POST /requests {}", itemRequestDto);
        return itemRequestDto;
    }

    @GetMapping
    public List<ItemRequestForGetDto> findAllByRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId)
            throws IncorrectObjectException {
        log.info("GET /requests by requester id={}", requesterId);
        return itemRequestService.getAllItemRequestsByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestForGetDto> findAll(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                              @RequestParam(required = false) Integer from,
                                              @RequestParam(required = false) Integer size)
            throws IncorrectObjectException {
        if (from == null && size == null) {
            log.info("GET /requests by requester id={}", requesterId);
            return itemRequestService.getAllItemRequests(requesterId);
        }
        log.info("GET /requests by requester id={}, from {} size {}", requesterId, from, size);
        return itemRequestService.getAllItemRequestsByPages(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestForGetDto findById(@RequestHeader(value = "X-Sharer-User-Id") Long requesterId,
                                         @PathVariable("requestId") Long requestId)
            throws IncorrectObjectException {
        log.info("GET /requests/" + requestId);
        return itemRequestService.getItemRequestById(requesterId, requestId);
    }
}
