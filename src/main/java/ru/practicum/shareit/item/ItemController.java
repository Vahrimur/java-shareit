package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;

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
            throws IncorrectObjectException {
        itemDto = itemService.updateItem(userId, itemDto, itemId);
        log.info("PATCH /items {}", itemDto);
        return itemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable("itemId") Long itemId) throws IncorrectObjectException {
        log.info("GET /items/" + itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items by owner id={}", userId);
        return itemService.getAllItemsByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam String text) {
        log.info("GET /items/search?text=" + text);
        return itemService.searchItemsByText(text);
    }
}
