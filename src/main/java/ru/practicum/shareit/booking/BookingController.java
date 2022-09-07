package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId,
                             @RequestBody BookingDto bookingDto)
            throws IncorrectObjectException, IncorrectFieldException {
        bookingDto = bookingService.addNewBooking(ownerId, bookingDto);
        log.info("POST /bookings {}", bookingDto);
        return bookingDto;
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoForUpdateAndGet changeStatus(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId,
                                                  @PathVariable("bookingId") Long bookingId, @RequestParam boolean approved)
            throws IncorrectObjectException, IncorrectFieldException {
        BookingDtoForUpdateAndGet booking = bookingService.changeBookingStatus(ownerId, bookingId, approved);
        log.info("PATCH /bookings/{}?approved={} by owner id={}", bookingId, approved, ownerId);
        return booking;
    }

    @GetMapping("/{bookingId}")
    public BookingDtoForUpdateAndGet findById(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                              @PathVariable("bookingId") Long bookingId)
            throws IncorrectObjectException, IncorrectFieldException {
        log.info("GET /bookings/" + bookingId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoForUpdateAndGet> findAllByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                           @RequestParam(defaultValue = "ALL") String state)
            throws IncorrectObjectException, IncorrectEnumException {
        log.info("GET /bookings {} by booker id={}", state, bookerId);
        return bookingService.getAllBookingsByBookerId(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoForUpdateAndGet> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                          @RequestParam(defaultValue = "ALL") String state)
            throws IncorrectObjectException, IncorrectEnumException {
        log.info("GET /bookings/owner {} by owner id={}", state, ownerId);
        return bookingService.getAllBookingsByOwnerId(ownerId, state);
    }
}
