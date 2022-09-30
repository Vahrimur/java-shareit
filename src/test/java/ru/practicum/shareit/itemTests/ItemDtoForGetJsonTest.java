package ru.practicum.shareit.itemTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoForGet;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoForGetJsonTest {
    @Autowired
    private JacksonTester<ItemDtoForGet> json;

    @Test
    void testItemDtoForGet() throws Exception {
        List<CommentDto> comments = new ArrayList<>();
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "updateName",
                LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30));
        comments.add(commentDto);

        BookingDto bookingDto = new BookingDto(1L,
                LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30),
                LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30),
                1L,"Отвертка",1L,BookingStatus.WAITING);

        ItemDtoForGet itemDto = new ItemDtoForGet(1L, "Дрель", "Простая дрель", true,
                bookingDto, null, comments, null);

        JsonContent<ItemDtoForGet> result = json.write(itemDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).hasJsonPath("$.comments");
        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(itemDto.getLastBooking().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo(bookingDto.getStart().toString());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end")
                .isEqualTo(bookingDto.getEnd().toString());
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.itemId")
                .isEqualTo(bookingDto.getItemId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.itemName")
                .isEqualTo(bookingDto.getItemName());
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId")
                .isEqualTo(bookingDto.getBookerId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.status")
                .isEqualTo(bookingDto.getStatus().toString());
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(result).extractingJsonPathArrayValue("$.comments").isInstanceOf(ArrayList.class);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id")
                .isEqualTo(itemDto.getComments().get(0).getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text")
                .isEqualTo(itemDto.getComments().get(0).getText());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName")
                .isEqualTo(itemDto.getComments().get(0).getAuthorName());
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created")
                .isEqualTo(itemDto.getComments().get(0).getCreated().toString());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(null);
    }
}
