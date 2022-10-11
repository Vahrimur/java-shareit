package ru.practicum.shareit.requestTest;

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
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    @MockBean
    private final ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    private final User requester = new User(1L, "user", "user@user.com");
    private final User requester2 = new User(2L, "user2", "user2@user.com");
    private final ItemRequest itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться дрелью",
            requester, LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30));
    private final ItemRequestDto itemRequestDto = ItemRequestMapper.mapToItemRequestDto(itemRequest);
    private final Item item = new Item(
            1L, "Дрель", "Простая дрель", true, 2L, 1L);
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final ItemRequestForGetDto itemRequestForGetDto = ItemRequestForGetMapper.mapToItemRequestForGetDto(
            itemRequest, List.of(itemDto));

    @Autowired
    public ItemRequestControllerTest(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

    @Test
    void shouldCreate() throws Exception {
        when(itemRequestService.addNewItemRequest(1L, itemRequestDto))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .addNewItemRequest(1L, itemRequestDto);
    }

    @Test
    void shouldCreateRequesterNotFound() throws Exception {
        when(itemRequestService.addNewItemRequest(2L, itemRequestDto))
                .thenThrow(IncorrectObjectException.class);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllByRequester() throws Exception {
        when(itemRequestService.getAllItemRequestsByRequesterId(1L))
                .thenReturn(List.of(itemRequestForGetDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestForGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestForGetDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestForGetDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items[0].id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(itemDto.getRequestId().intValue())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getAllItemRequestsByRequesterId(1L);
    }

    @Test
    void shouldFindAllByRequesterNotFound() throws Exception {
        when(itemRequestService.getAllItemRequestsByRequesterId(2L))
                .thenThrow(IncorrectObjectException.class);

        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAll() throws Exception {
        when(itemRequestService.getAllItemRequests(2L, null, null))
                .thenReturn(List.of(itemRequestForGetDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestForGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestForGetDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestForGetDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items[0].id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(itemDto.getRequestId().intValue())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getAllItemRequests(2L, null, null);
    }

    @Test
    void shouldFindAllByPages() throws Exception {
        when(itemRequestService.getAllItemRequests(2L, 1, 1))
                .thenReturn(List.of(itemRequestForGetDto));

        mvc.perform(get("/requests/all?from=1&size=1")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestForGetDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestForGetDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestForGetDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items[0].id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(itemDto.getRequestId().intValue())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getAllItemRequests(2L, 1, 1);
    }

    @Test
    void shouldFindById() throws Exception {
        when(itemRequestService.getItemRequestById(1L, 1L))
                .thenReturn(itemRequestForGetDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestForGetDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestForGetDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestForGetDto.getCreated().toString())))
                .andExpect(jsonPath("$.items[0].id", is(itemDto.getId().intValue())))
                .andExpect(jsonPath("$.items[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.items[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.items[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.items[0].requestId", is(itemDto.getRequestId().intValue())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getItemRequestById(1L, 1L);
    }
}
