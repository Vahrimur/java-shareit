package ru.practicum.shareit.bookingTests;

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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    @MockBean
    private final BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    private final Item item = new Item(
            1L, "Дрель", "Простая дрель", true, 1L, null);
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final User booker = new User(2L, "name", "email@email.ru");
    private final UserDto bookerDto = UserMapper.mapToUserDto(booker);
    private final BookingDto bookingDto = new BookingDto(1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            1L, "Дрель", 2L, BookingStatus.WAITING);
    private final BookingDtoForUpdateAndGet bookingDtoForUpdateAndGet = new BookingDtoForUpdateAndGet(1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            itemDto, bookerDto, BookingStatus.WAITING);

    @Autowired
    public BookingControllerTest(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.addNewBooking(1L, bookingDto))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.itemName", is(bookingDto.getItemName())))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .addNewBooking(1L, bookingDto);
    }

    @Test
    void shouldChangeStatus() throws Exception {
        when(bookingService.changeBookingStatus(1L, 1L, true))
                .thenReturn(bookingDtoForUpdateAndGet);

        mvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$.item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$.booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$.status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .changeBookingStatus(1L, 1L, true);
    }

    @Test
    void shouldFindById() throws Exception {
        when(bookingService.getBookingById(1L, 1L))
                .thenReturn(bookingDtoForUpdateAndGet);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$.item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$.item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$.booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$.booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$.status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getBookingById(1L, 1L);
    }

    @Test
    void shouldFindAllByBooker() throws Exception {
        when(bookingService.getAllBookingsByBookerId(2L, State.ALL.toString(), null, null))
                .thenReturn(List.of(bookingDtoForUpdateAndGet));

        mvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id",
                        is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$[0].booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getAllBookingsByBookerId(2L, State.ALL.toString(), null, null);
    }

    @Test
    void shouldFindAllByBookerByPages() throws Exception {
        when(bookingService.getAllBookingsByBookerId(2L, State.ALL.toString(), 1, 1))
                .thenReturn(List.of(bookingDtoForUpdateAndGet));

        mvc.perform(get("/bookings?state=ALL&from=1&size=1")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id",
                        is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$[0].booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getAllBookingsByBookerId(2L, State.ALL.toString(), 1, 1);
    }

    @Test
    void shouldFindAllByOwner() throws Exception {
        when(bookingService.getAllBookingsByOwnerId(1L, State.ALL.toString(), null, null))
                .thenReturn(List.of(bookingDtoForUpdateAndGet));

        mvc.perform(get("/bookings/owner?state=ALL")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id",
                        is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$[0].booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getAllBookingsByOwnerId(1L, State.ALL.toString(), null, null);
    }

    @Test
    void shouldFindAllByOwnerByPages() throws Exception {
        when(bookingService.getAllBookingsByOwnerId(1L, State.ALL.toString(), 1, 1))
                .thenReturn(List.of(bookingDtoForUpdateAndGet));

        mvc.perform(get("/bookings/owner?state=ALL&from=1&size=1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDtoForUpdateAndGet.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDtoForUpdateAndGet.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDtoForUpdateAndGet.getEnd().toString())))
                .andExpect(jsonPath("$[0].item.id",
                        is(bookingDtoForUpdateAndGet.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingDtoForUpdateAndGet.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description",
                        is(bookingDtoForUpdateAndGet.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available",
                        is(bookingDtoForUpdateAndGet.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].item.requestId",
                        is(bookingDtoForUpdateAndGet.getItem().getRequestId())))
                .andExpect(jsonPath("$[0].booker.id",
                        is(bookingDtoForUpdateAndGet.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name",
                        is(bookingDtoForUpdateAndGet.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email",
                        is(bookingDtoForUpdateAndGet.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].status", is(bookingDtoForUpdateAndGet.getStatus().toString())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getAllBookingsByOwnerId(1L, State.ALL.toString(), 1, 1);
    }
}
