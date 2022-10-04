package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.List;

public class BookingForUpdateAndGetMapper {
    public static BookingDtoForUpdateAndGet mapToBookingDto(Booking booking) {
        return new BookingDtoForUpdateAndGet(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.mapToItemDto(booking.getItem()),
                UserMapper.mapToUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public static List<BookingDtoForUpdateAndGet> mapToBookingDto(Iterable<Booking> bookings) {
        List<BookingDtoForUpdateAndGet> dtos = new ArrayList<>();
        for (Booking booking : bookings) {
            dtos.add(mapToBookingDto(booking));
        }
        return dtos;
    }

    public static Booking mapToBookingEntity(BookingDtoForUpdateAndGet bookingDto, Long ownerId) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                ItemMapper.mapToItemEntity(bookingDto.getItem(), ownerId),
                UserMapper.mapToUserEntity(bookingDto.getBooker()),
                bookingDto.getStatus()
        );
    }

    public static List<Booking> mapToBookingEntity(Iterable<BookingDtoForUpdateAndGet> bookingDtos, Long ownerId) {
        List<Booking> bookings = new ArrayList<>();
        for (BookingDtoForUpdateAndGet booking : bookingDtos) {
            bookings.add(mapToBookingEntity(booking, ownerId));
        }
        return bookings;
    }
}
