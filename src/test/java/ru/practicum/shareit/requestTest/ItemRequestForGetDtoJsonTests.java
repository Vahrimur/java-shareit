package ru.practicum.shareit.requestTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.request.dto.ItemRequestForGetDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestForGetDtoJsonTests {

    @Autowired
    private JacksonTester<ItemRequestForGetDto> json;

    @Test
    void testItemRequestForGetDto() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        ItemDto itemDto1 = new ItemDto(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L
        );
        ItemDto itemDto2 = new ItemDto(
                2L,
                "Дрель б/у",
                "Простая дрель б/у",
                true,
                1L
        );
        items.add(itemDto1);
        items.add(itemDto2);

        ItemRequestForGetDto itemRequestForGetDto = new ItemRequestForGetDto(
                2L,
                "Хотел бы воспользоваться любой дрелью",
                LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30),
                items
        );

        JsonContent<ItemRequestForGetDto> result = json.write(itemRequestForGetDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");
        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemRequestForGetDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestForGetDto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(itemRequestForGetDto.getCreated().toString());
        assertThat(result).extractingJsonPathArrayValue("$.items").isInstanceOf(ArrayList.class);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(itemRequestForGetDto.getItems().get(0).getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo(itemRequestForGetDto.getItems().get(0).getName());
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo(itemRequestForGetDto.getItems().get(0).getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available")
                .isEqualTo(itemRequestForGetDto.getItems().get(0).getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId")
                .isEqualTo(itemRequestForGetDto.getItems().get(0).getRequestId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id")
                .isEqualTo(itemRequestForGetDto.getItems().get(1).getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[1].name")
                .isEqualTo(itemRequestForGetDto.getItems().get(1).getName());
        assertThat(result).extractingJsonPathStringValue("$.items[1].description")
                .isEqualTo(itemRequestForGetDto.getItems().get(1).getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.items[1].available")
                .isEqualTo(itemRequestForGetDto.getItems().get(1).getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.items[1].requestId")
                .isEqualTo(itemRequestForGetDto.getItems().get(1).getRequestId().intValue());
    }
}
