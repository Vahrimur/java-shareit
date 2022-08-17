package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectObjectException, IncorrectFieldException;

    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) throws IncorrectObjectException;

    ItemDto getItemById(Long itemId) throws IncorrectObjectException;

    List<ItemDto> getAllItemsByUserId(Long userId);

    List<ItemDto> searchItemsByText(String text);
}
