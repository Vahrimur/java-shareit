package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;

import java.util.List;

public interface BookingService {
    BookingDto addNewBooking(Long userId, BookingDto bookingDto)
            throws IncorrectObjectException, IncorrectFieldException;

    BookingDtoForUpdateAndGet changeBookingStatus(Long ownerId, Long bookingId, boolean approved)
            throws IncorrectObjectException, IncorrectFieldException;

    BookingDtoForUpdateAndGet getBookingById(Long bookingId, Long userId)
            throws IncorrectObjectException, IncorrectFieldException;

    List<BookingDtoForUpdateAndGet> getAllBookingsByBookerId(Long bookerId, String state)
            throws IncorrectObjectException, IncorrectEnumException;

    List<BookingDtoForUpdateAndGet> getAllBookingsByOwnerId(Long ownerId, String state)
            throws IncorrectObjectException, IncorrectEnumException;

    void checkCorrectItemBookerAndBookingEnded(Long userId, Long itemId)
            throws IncorrectObjectException, IncorrectFieldException;
}
