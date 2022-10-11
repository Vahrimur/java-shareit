package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(long userId, BookItemRequestDto requestDto)
            throws IncorrectFieldException {
        checkCorrectTime(requestDto);
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> changeStatus(Long ownerId, Long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved=" + approved, ownerId, parameters);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByBooker(long bookerId, String state, Integer from, Integer size)
            throws IncorrectEnumException {
        checkState(state);
        if ((from == null) || (size == null)) {
            return get("?state=" + state, bookerId);
        }
        checkPageableParams(from, size);
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", bookerId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwner(Long ownerId, String state, Integer from, Integer size)
            throws IncorrectEnumException {
        checkState(state);
        if ((from == null) || (size == null)) {
            return get("/owner?state=" + state, ownerId);
        }
        checkPageableParams(from, size);
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }

    private void checkCorrectTime(BookItemRequestDto booking) throws IncorrectFieldException {
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("The end of the booking cannot be in the past");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectFieldException("The end of the booking cannot be earlier than the beginning");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("The beginning of the booking cannot be in the past");
        }
    }

    private void checkState(String state) throws IncorrectEnumException {
        if (Arrays.stream(State.values()).noneMatch((st) -> st.name().equals(state))) {
            throw new IncorrectEnumException("Unknown state: " + state);
        }
    }

    private void checkPageableParams(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Index of start element cannot be less zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal zero");
        }
    }
}
