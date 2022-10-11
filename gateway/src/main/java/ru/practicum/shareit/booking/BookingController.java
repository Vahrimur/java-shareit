package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestBody @Valid BookItemRequestDto bookingDto)
            throws IncorrectFieldException {
        log.info("Creating booking {}, userId={}", bookingDto, userId);
        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId,
                                               @PathVariable("bookingId") Long bookingId,
                                               @RequestParam boolean approved) {
        log.info("Updating booking id={}, approved={} by owner id={}", bookingId, approved, ownerId);
        return bookingClient.changeStatus(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable("bookingId") Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(required = false) Integer from,
                                                      @RequestParam(required = false) Integer size)
            throws IncorrectEnumException {
        log.info("Get booking with state {}, bookerId={}, from={}, size={}", state, bookerId, from, size);
        return bookingClient.getBookingsByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(required = false) Integer from,
                                                     @RequestParam(required = false) Integer size)
            throws IncorrectEnumException {
        log.info("Get booking with state {}, ownerId={}, from={}, size={}", state, ownerId, from, size);
        return bookingClient.getBookingsByOwner(ownerId, state, from, size);
    }
}
