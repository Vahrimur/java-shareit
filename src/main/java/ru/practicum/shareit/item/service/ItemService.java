package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.IncorrectItemException;
import ru.practicum.shareit.exception.IncorrectUserException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectUserException, ValidationException;
    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) throws IncorrectUserException, IncorrectItemException;
    ItemDto getItemById(Long itemId) throws IncorrectItemException;
    List<ItemDto> getAllItemsByUserId(Long userId);
    List<ItemDto> searchItemsByText(String text);
}
