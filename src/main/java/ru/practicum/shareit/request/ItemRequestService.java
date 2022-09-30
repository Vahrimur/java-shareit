package ru.practicum.shareit.request;

import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addNewItemRequest(Long requesterId, ItemRequestDto itemRequestDto)
            throws IncorrectObjectException, IncorrectFieldException;

    List<ItemRequestForGetDto> getAllItemRequestsByRequesterId(Long requesterId) throws IncorrectObjectException;

    List<ItemRequestForGetDto> getAllItemRequests(Long requesterId, Integer from, Integer size)
            throws IncorrectObjectException;

    ItemRequestForGetDto getItemRequestById(Long requesterId, Long itemRequestId) throws IncorrectObjectException;
}
