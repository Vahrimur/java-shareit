package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForGet;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectObjectException, IncorrectFieldException;

    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId)
            throws IncorrectObjectException, IncorrectFieldException;

    ItemDtoForGet getItemById(Long itemId, Long userId) throws IncorrectObjectException, IncorrectFieldException;

    List<ItemDtoForGet> getAllItemsByUserId(Long userId, Integer from, Integer size)
            throws IncorrectObjectException;

    List<ItemDto> searchItemsByText(String text, Integer from, Integer size, Long userId)
            throws IncorrectObjectException;

    void checkItemExist(Long id) throws IncorrectObjectException, IncorrectFieldException;

    void checkItemAvailable(Long id) throws IncorrectFieldException;

    void checkCorrectItemOwner(Long itemId, Long userId) throws IncorrectObjectException;

    void checkItemOwner(Long itemId, Long userId) throws IncorrectObjectException;

    CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto)
            throws IncorrectObjectException, IncorrectFieldException;
}
