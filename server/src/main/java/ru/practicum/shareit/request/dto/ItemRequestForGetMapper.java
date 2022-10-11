package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;

public class ItemRequestForGetMapper {

    public static ItemRequestForGetDto mapToItemRequestForGetDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestForGetDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items
        );
    }

    public static ItemRequest mapToItemRequestEntity(ItemRequestForGetDto itemRequestForGetDto, User requester) {
        return new ItemRequest(
                itemRequestForGetDto.getId(),
                itemRequestForGetDto.getDescription(),
                requester,
                itemRequestForGetDto.getCreated()
        );
    }

    public static List<ItemRequest> mapToItemRequestEntity(
            Iterable<ItemRequestForGetDto> itemRequestForGetDtos, User requester) {
        List<ItemRequest> requests = new ArrayList<>();
        for (ItemRequestForGetDto request : itemRequestForGetDtos) {
            requests.add(mapToItemRequestEntity(request, requester));
        }
        return requests;
    }
}
