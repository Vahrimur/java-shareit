package ru.practicum.shareit.itemTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForGet;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    @MockBean
    private final ItemService itemService;
    @Autowired
    private MockMvc mvc;

    private final Item item = new Item(
            1L, "Дрель", "Простая дрель", true, 1L, null);
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final CommentDto commentDto = new CommentDto(1L, "Add comment from booker", "name2",
            LocalDateTime.of(2022, Month.OCTOBER, 18, 12, 30, 30));
    private final List<CommentDto> comments = List.of(commentDto);
    private final ItemDtoForGet itemDtoForGet = new ItemDtoForGet(1L, "Дрель", "Простая дрель",
            true, null, null, comments, null);

    @Autowired
    public ItemControllerTest(ItemService itemService) {
        this.itemService = itemService;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.addNewItem(1L, itemDto))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        Mockito.verify(itemService, Mockito.times(1))
                .addNewItem(1L, itemDto);
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.updateItem(1L, itemDto, 1L))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        Mockito.verify(itemService, Mockito.times(1))
                .updateItem(1L, itemDto, 1L);
    }

    @Test
    void shouldFindItemById() throws Exception {
        when(itemService.getItemById(1L, 1L))
                .thenReturn(itemDtoForGet);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoForGet.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoForGet.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoForGet.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoForGet.getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(itemDtoForGet.getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemDtoForGet.getNextBooking())))
                .andExpect(jsonPath("$.comments[0].id",
                        is(itemDtoForGet.getComments().get(0).getId().intValue())))
                .andExpect(jsonPath("$.comments[0].text", is(itemDtoForGet.getComments().get(0).getText())))
                .andExpect(jsonPath("$.comments[0].authorName",
                        is(itemDtoForGet.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created",
                        is(itemDtoForGet.getComments().get(0).getCreated().toString())))
                .andExpect(jsonPath("$.requestId", is(itemDtoForGet.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1))
                .getItemById(1L, 1L);
    }

    @Test
    void shouldFindAllItems() throws Exception {
        when(itemService.getAllItemsByUserId(1L, null, null))
                .thenReturn(List.of(itemDtoForGet));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoForGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoForGet.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoForGet.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoForGet.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", is(itemDtoForGet.getLastBooking())))
                .andExpect(jsonPath("$[0].nextBooking", is(itemDtoForGet.getNextBooking())))
                .andExpect(jsonPath("$[0].comments[0].id",
                        is(itemDtoForGet.getComments().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].comments[0].text", is(itemDtoForGet.getComments().get(0).getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName",
                        is(itemDtoForGet.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created",
                        is(itemDtoForGet.getComments().get(0).getCreated().toString())))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoForGet.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1))
                .getAllItemsByUserId(1L, null, null);
    }

    @Test
    void shouldFindAllItemsByPages() throws Exception {
        when(itemService.getAllItemsByUserId(1L, 1, 1))
                .thenReturn(List.of(itemDtoForGet));

        mvc.perform(get("/items?from=1&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoForGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoForGet.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoForGet.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoForGet.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", is(itemDtoForGet.getLastBooking())))
                .andExpect(jsonPath("$[0].nextBooking", is(itemDtoForGet.getNextBooking())))
                .andExpect(jsonPath("$[0].comments[0].id",
                        is(itemDtoForGet.getComments().get(0).getId().intValue())))
                .andExpect(jsonPath("$[0].comments[0].text", is(itemDtoForGet.getComments().get(0).getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName",
                        is(itemDtoForGet.getComments().get(0).getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created",
                        is(itemDtoForGet.getComments().get(0).getCreated().toString())))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoForGet.getRequestId())));

        Mockito.verify(itemService, Mockito.times(1))
                .getAllItemsByUserId(1L, 1, 1);
    }

    @Test
    void shouldSearchItemByText() throws Exception {
        when(itemService.searchItemsByText("Дрель", null, null, 1L))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=Дрель")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));

        Mockito.verify(itemService, Mockito.times(1))
                .searchItemsByText("Дрель", null, null, 1L);
    }

    @Test
    void shouldSearchItemByTextByPages() throws Exception {
        when(itemService.searchItemsByText("Дрель", 1, 1, 1L))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=Дрель&from=1&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));

        Mockito.verify(itemService, Mockito.times(1))
                .searchItemsByText("Дрель", 1, 1, 1L);
    }

    @Test
    void shouldCreateComment() throws Exception {
        when(itemService.addNewComment(2L, 1L, commentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().toString())));

        Mockito.verify(itemService, Mockito.times(1))
                .addNewComment(2L, 1L, commentDto);
    }
}
