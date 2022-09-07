package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemForGetMapper {
    public static ItemDtoForGet mapToItemDto(Item item,
                                             BookingDto lastBooking,
                                             BookingDto nextBooking,
                                             List<CommentDto> comments) {
        return new ItemDtoForGet(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments,
                item.getRequestId() != null ? item.getRequestId() : null
        );
    }

    public static Item mapToItemEntity(ItemDtoForGet itemDto, Long ownerId) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                itemDto.getRequestId() != null ? itemDto.getRequestId() : null
        );
    }
}
