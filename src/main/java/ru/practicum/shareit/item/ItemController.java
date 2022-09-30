package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForGet;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto itemDto) throws IncorrectObjectException, IncorrectFieldException {
        itemDto = itemService.addNewItem(userId, itemDto);
        log.info("POST /items {}", itemDto);
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto itemDto, @PathVariable("itemId") Long itemId)
            throws IncorrectObjectException, IncorrectFieldException {
        itemDto = itemService.updateItem(userId, itemDto, itemId);
        log.info("PATCH /items {}", itemDto);
        return itemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDtoForGet findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable("itemId") Long itemId)
            throws IncorrectObjectException, IncorrectFieldException {
        log.info("GET /items/" + itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoForGet> findAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(required = false) Integer from,
                                       @RequestParam(required = false) Integer size) throws IncorrectObjectException {
        log.info("GET /items by owner id={}, from {} size {}", userId, from, size);
        return itemService.getAllItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam String text,
                                      @RequestParam(required = false) Integer from,
                                      @RequestParam(required = false) Integer size) {
        log.info("GET /items/search?text={}&from={}&size={}", text, from, size);
        return itemService.searchItemsByText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto create(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                             @PathVariable("itemId") Long itemId,
                             @RequestBody CommentDto commentDto)
            throws IncorrectObjectException, IncorrectFieldException {
        commentDto = itemService.addNewComment(userId, itemId, commentDto);
        log.info("POST /{}/comment by user id={}", itemId, userId);
        return commentDto;
    }
}
