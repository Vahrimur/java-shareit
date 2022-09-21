package ru.practicum.shareit.bookingTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoForUpdateAndGetJsonTest {
    @Autowired
    private JacksonTester<BookingDtoForUpdateAndGet> json;

    @Test
    void testBookingDtoForUpdateAndGetDto() throws Exception {
        ItemDto item = new ItemDto(
                1L, "Дрель", "Простая дрель", true, null);

        UserDto booker = new UserDto(1L, "user", "user@user.com");

        BookingDtoForUpdateAndGet bookingDto = new BookingDtoForUpdateAndGet(1L,
                LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30),
                LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30),
                item, booker, BookingStatus.WAITING);

        JsonContent<BookingDtoForUpdateAndGet> result = json.write(bookingDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).hasJsonPath("$.item");
        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).hasJsonPath("$.status");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(bookingDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(bookingDto.getEnd().toString());
        assertThat(result).extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(bookingDto.getItem().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name")
                .isEqualTo(bookingDto.getItem().getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description")
                .isEqualTo(bookingDto.getItem().getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available")
                .isEqualTo(bookingDto.getItem().getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId")
                .isEqualTo(bookingDto.getItem().getRequestId());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(bookingDto.getBooker().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name")
                .isEqualTo(bookingDto.getBooker().getName());
        assertThat(result).extractingJsonPathStringValue("$.booker.email")
                .isEqualTo(bookingDto.getBooker().getEmail());
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().toString());
    }
}
