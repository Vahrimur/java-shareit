package ru.practicum.shareit.bookingTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    private BookingService bookingService;
    private MockitoSession session;

    private final Item item = new Item(
            1L, "Дрель", "Простая дрель", true, 1L, null);
    private final ItemDto itemDto = new ItemDto(
            1L, "Дрель", "Простая дрель", true, null);
    private final User booker = new User(2L, "name", "email@email.ru");
    private final UserDto bookerDto = new UserDto(2L, "name", "email@email.ru");
    private final BookingDto bookingDto = new BookingDto(1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            1L, "Дрель", 2L, BookingStatus.WAITING);
    private final BookingDtoForUpdateAndGet bookingDtoForUpdateAndGet = new BookingDtoForUpdateAndGet(1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            itemDto, bookerDto, BookingStatus.WAITING);
    private final Booking booking = new Booking(1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            item, booker, BookingStatus.WAITING);

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        bookingService = new BookingServiceImpl(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    void shouldAddNewBooking() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(userRepository.getById(2L))
                .thenReturn(booker);
        Mockito
                .when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto newBooking = bookingService.addNewBooking(2L, bookingDto);

        Assertions.assertEquals(bookingDto.getId(), newBooking.getId());
        Assertions.assertEquals(bookingDto.getStart(), newBooking.getStart());
        Assertions.assertEquals(bookingDto.getEnd(), newBooking.getEnd());
        Assertions.assertEquals(bookingDto.getItemId(), newBooking.getItemId());
        Assertions.assertEquals(bookingDto.getItemName(), newBooking.getItemName());
        Assertions.assertEquals(bookingDto.getBookerId(), newBooking.getBookerId());
        Assertions.assertEquals(bookingDto.getStatus(), newBooking.getStatus());

        Mockito
                .verify(itemRepository, Mockito.times(2))
                .getById(1L);
        Mockito
                .verify(userRepository, Mockito.times(2))
                .getById(2L);
        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(itemService, Mockito.times(1))
                .checkItemExist(1L);
        Mockito
                .verify(itemService, Mockito.times(1))
                .checkItemOwner(1L, 2L);
        Mockito
                .verify(itemService, Mockito.times(1))
                .checkItemAvailable(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .save(booking);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldChangeBookingStatusApproved() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(bookingRepository.findById(any()))
                .thenReturn(Optional.ofNullable(booking));
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(List.of(booking));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDtoForUpdateAndGet bookingUpdated = bookingService
                .changeBookingStatus(1L, 1L, true);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookingUpdated.getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookingUpdated.getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookingUpdated.getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookingUpdated.getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookingUpdated.getBooker());
        Assertions.assertEquals(BookingStatus.APPROVED, bookingUpdated.getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findById(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verify(itemService, Mockito.times(1))
                .checkCorrectItemOwner(1L, 1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .save(booking);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldChangeBookingStatusRejected() throws Exception {
        Mockito
                .when(bookingRepository.findById(any()))
                .thenReturn(Optional.ofNullable(booking));
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(List.of(booking));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(bookingRepository.save(any()))
                .thenReturn(booking);

        booking.setStatus(BookingStatus.APPROVED);
        BookingDtoForUpdateAndGet bookingUpdated2 = bookingService
                .changeBookingStatus(1L, 1L, false);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookingUpdated2.getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookingUpdated2.getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookingUpdated2.getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookingUpdated2.getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookingUpdated2.getBooker());
        Assertions.assertEquals(BookingStatus.REJECTED, bookingUpdated2.getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findById(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verify(itemService, Mockito.times(1))
                .checkCorrectItemOwner(1L, 1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .save(booking);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldChangeBookingStatusNoBookings() throws IncorrectObjectException {
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(new ArrayList<>());

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> bookingService.changeBookingStatus(1L, 99L, true));
        Assertions.assertEquals("There is no booking with such ID", exception.getMessage());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldChangeBookingStatusSameStatus() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);

        Mockito
                .when(bookingRepository.findById(any()))
                .thenReturn(Optional.ofNullable(booking));
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(List.of(booking));

        final IncorrectFieldException exception = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.changeBookingStatus(1L, 1L, true));
        Assertions.assertEquals("It is not possible to update the booking status to the same",
                exception.getMessage());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetBookingById() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(List.of(booking));
        Mockito
                .when(bookingRepository.findById(any()))
                .thenReturn(Optional.ofNullable(booking));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(bookingRepository.getById(1L))
                .thenReturn(booking);

        BookingDtoForUpdateAndGet bookingGot = bookingService.getBookingById(1L, 1L);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookingGot.getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookingGot.getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookingGot.getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookingGot.getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookingGot.getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookingGot.getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(bookingRepository, Mockito.times(2))
                .findById(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetBookingByIdWrongUser() {
        Mockito
                .when(bookingRepository.findAll())
                .thenReturn(List.of(booking));
        Mockito
                .when(bookingRepository.findById(any()))
                .thenReturn(Optional.ofNullable(booking));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> bookingService.getBookingById(1L, 99L));
        Assertions.assertEquals("Incorrect item owner or booker ID is specified",
                exception.getMessage());
    }

    @Test
    void shouldGetAllBookingsByBookerIdAll() throws IncorrectObjectException, IncorrectEnumException {
        Mockito
                .when(bookingRepository.findAllByBookerId(2L))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.ALL), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerId(2L);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdRejected() throws IncorrectObjectException, IncorrectEnumException {
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatus(2L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.REJECTED), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatus(2L, BookingStatus.REJECTED);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdByPagesWaiting() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1), 1, Sort.by("start").descending());

        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusByPages(2L, BookingStatus.WAITING, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.WAITING), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusByPages(2L, BookingStatus.WAITING, sorted);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );
    }

    @Test
    void shouldGetAllBookingsByBookerIdByPagesAll() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1), 1, Sort.by("start").descending());

        Mockito
                .when(bookingRepository.findAllByBookerIdByPages(2L, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.ALL), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdByPages(2L, sorted);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdByPagesRejected() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1), 1, Sort.by("start").descending());
        booking.setStatus(BookingStatus.REJECTED);

        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusByPages(2L, BookingStatus.REJECTED, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.REJECTED), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusByPages(2L, BookingStatus.REJECTED, sorted);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdCurrent() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdCurrent(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.CURRENT), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdCurrent(anyLong(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdCurrentByPages() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdCurrentByPages(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.CURRENT), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdCurrentByPages(anyLong(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdFuture() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 10, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdFuture(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.FUTURE), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdFuture(anyLong(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdFutureByPages() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 10, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdFutureByPages(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.FUTURE), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdFutureByPages(anyLong(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdPast() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.SEPTEMBER, 10, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdPast(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.PAST), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdPast(anyLong(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByBookerIdPastByPages() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.SEPTEMBER, 10, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(bookingRepository.findAllByBookerIdPastByPages(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerId(2L, String.valueOf(State.PAST), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdPastByPages(anyLong(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesRejected() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1), 1, Sort.by("start").descending());
        booking.setStatus(BookingStatus.REJECTED);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsAndStatusByPages(List.of(item), BookingStatus.REJECTED, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(BookingStatus.REJECTED), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsAndStatusByPages(List.of(item), BookingStatus.REJECTED, sorted);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdAll() throws IncorrectObjectException, IncorrectEnumException {
        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItems(List.of(item)))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.ALL), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItems(List.of(item));
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdRejected() throws IncorrectObjectException, IncorrectEnumException {
        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsAndStatus(List.of(item), BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.REJECTED), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsAndStatus(List.of(item), BookingStatus.REJECTED);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesAll() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1), 1, Sort.by("start").descending());

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsByPages(List.of(item), sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.ALL), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsByPages(List.of(item), sorted);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdCurrent() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsCurrent(anyList(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.CURRENT), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsCurrent(anyList(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdFuture() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsFuture(anyList(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.FUTURE), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsFuture(anyList(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdPast() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsPast(anyList(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.PAST), null, null);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsPast(anyList(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesCurrent() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsCurrentByPages(anyList(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.CURRENT), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsCurrentByPages(anyList(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesFuture() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.NOVEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.NOVEMBER, 9, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsFutureByPages(anyList(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.FUTURE), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsFutureByPages(anyList(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesPast() throws IncorrectObjectException, IncorrectEnumException {
        LocalDateTime start = LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30);
        booking.setEnd(end);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsPastByPages(anyList(), any(), any()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.PAST), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(end, bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemsPastByPages(anyList(), any(), any());
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }

    @Test
    void shouldCheckCorrectItemBookerAndBookingEndedWrongBooker() {
        booking.setStart(LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30));

        Mockito
                .when(bookingRepository.findAllByItemIdAndBookerId(1L, 1L))
                .thenReturn(List.of(booking));

        final IncorrectFieldException exception2 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.checkCorrectItemBookerAndBookingEnded(1L, 1L));
        Assertions.assertEquals("Incorrect item booker ID is specified", exception2.getMessage());

        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndBookerId(1L, 1L);
        Mockito
                .verifyNoMoreInteractions(
                        bookingRepository,
                        itemRepository,
                        userRepository,
                        userService,
                        itemService
                );
    }
}
