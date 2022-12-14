package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody @Valid ItemDto itemDto) throws IncorrectFieldException {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid ItemDto itemDto,
                                         @PathVariable("itemId") Long itemId) {
        log.info("Updating item {}", itemDto);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable("itemId") Long itemId) {
        log.info("Get item " + itemId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(required = false) Integer from,
                                                   @RequestParam(required = false) Integer size) {
        log.info("Get items by owner id={}, from {} size {}", userId, from, size);
        return itemClient.getAllItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByText(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam String text,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        log.info("Get items by text {}, from {} size {}", text, from, size);
        return itemClient.getAllItemsByByText(text, from, size, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                @PathVariable("itemId") Long itemId,
                                                @RequestBody CommentDto commentDto) throws IncorrectFieldException {
        log.info("Creating comment for item {} by user id={}", itemId, userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
