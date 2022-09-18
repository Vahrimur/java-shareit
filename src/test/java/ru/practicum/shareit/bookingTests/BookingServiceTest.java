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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

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
    BookingService bookingService;
    private MockitoSession session;

    private Item item = new Item(1L, "Дрель", "Простая дрель", true, 1L, null);
    private ItemDto itemDto = new ItemDto(1L, "Дрель", "Простая дрель", true, null);
    private User owner = new User(1L, "user", "user@user.com");
    private User booker = new User(2L, "name", "email@email.ru");
    private UserDto bookerDto = new UserDto(2L, "name", "email@email.ru");

    BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            1L,
            "Дрель",
            2L,
            BookingStatus.WAITING
    );

    BookingDtoForUpdateAndGet bookingDtoForUpdateAndGet = new BookingDtoForUpdateAndGet(
            1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            itemDto,
            bookerDto,
            BookingStatus.WAITING
    );

    Booking booking = new Booking(
            1L,
            LocalDateTime.of(2022, Month.OCTOBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.OCTOBER, 9, 12, 30, 30),
            item,
            booker,
            BookingStatus.WAITING
    );

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

        Mockito.verify(itemRepository, Mockito.times(2))
                .getById(1L);
        Mockito.verify(userRepository, Mockito.times(2))
                .getById(2L);
        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito.verify(itemService, Mockito.times(1))
                .checkItemExist(1L);
        Mockito.verify(itemService, Mockito.times(1))
                .checkItemOwner(1L, 2L);
        Mockito.verify(itemService, Mockito.times(1))
                .checkItemAvailable(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(booking);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );

        bookingDto.setEnd(LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30));
        final IncorrectFieldException exception1 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.addNewBooking(2L, bookingDto));
        Assertions.assertEquals("The end of the booking cannot be in the past", exception1.getMessage());

        bookingDto.setEnd(LocalDateTime.of(2022, Month.OCTOBER, 7, 12, 30, 30));
        final IncorrectFieldException exception2 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.addNewBooking(2L, bookingDto));
        Assertions.assertEquals("The end of the booking cannot be earlier than the beginning",
                exception2.getMessage());

        bookingDto.setStart(LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30));
        final IncorrectFieldException exception3 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.addNewBooking(2L, bookingDto));
        Assertions.assertEquals("The beginning of the booking cannot be in the past", exception3.getMessage());
    }

    @Test
    void shouldChangeBookingStatus() throws IncorrectObjectException, IncorrectFieldException {
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

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito.verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito.verify(itemService, Mockito.times(1))
                .checkCorrectItemOwner(1L, 1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(booking);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );

        final IncorrectObjectException exception1 = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> bookingService.changeBookingStatus(1L, 99L, true));
        Assertions.assertEquals("There is no booking with such ID", exception1.getMessage());

        booking.setStatus(BookingStatus.REJECTED);
        final IncorrectFieldException exception2 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.changeBookingStatus(1L, 1L, false));
        Assertions.assertEquals("It is not possible to update the booking status to the same",
                exception2.getMessage());
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

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito.verify(bookingRepository, Mockito.times(2))
                .findAll();
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .getById(1L);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );

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
                .getAllBookingsByBookerId(2L, String.valueOf(State.ALL));

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerId(2L);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );
    }

    @Test
    void shouldCheckState() {
        final IncorrectEnumException exception = Assertions.assertThrows(
                IncorrectEnumException.class,
                () -> bookingService.getAllBookingsByBookerId(2L, "SOME"));

        Assertions.assertEquals("Unknown state: SOME",
                exception.getMessage());
    }

    @Test
    void shouldGetAllBookingsByBookerIdByPagesWaiting() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1 / 1), 1, Sort.by("start").descending());

        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusByPages(2L, BookingStatus.WAITING, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByBookerIdByPages(2L, String.valueOf(State.WAITING), 1, 1);

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
    void shouldCheckPageableParams() {
        final IllegalArgumentException exception1 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookingService
                        .getAllBookingsByBookerIdByPages(2L, String.valueOf(State.WAITING), -1, 1));

        Assertions.assertEquals("Index of start element cannot be less zero",exception1.getMessage());

        final IllegalArgumentException exception2 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> bookingService
                        .getAllBookingsByBookerIdByPages(2L, String.valueOf(State.WAITING), 1, 0));

        Assertions.assertEquals("Page size cannot be less or equal zero",exception2.getMessage());
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPagesRejected() throws IncorrectObjectException, IncorrectEnumException {
        Pageable sorted = PageRequest.of((1 / 1), 1, Sort.by("start").descending());
        booking.setStatus(BookingStatus.REJECTED);

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsAndStatusByPages(List.of(item), BookingStatus.REJECTED, sorted))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerIdByPages(1L, String.valueOf(BookingStatus.REJECTED), 1, 1);

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStart(), bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(BookingStatus.REJECTED, bookings.get(0).getStatus());

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemsAndStatusByPages(List.of(item), BookingStatus.REJECTED, sorted);
        Mockito.verifyNoMoreInteractions(
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

        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(bookingRepository.findAllByItemsCurrent(List.of(item), LocalDateTime.now()))
                .thenReturn(List.of(booking));

        List<BookingDtoForUpdateAndGet> bookings = bookingService
                .getAllBookingsByOwnerId(1L, String.valueOf(State.CURRENT));

        Assertions.assertEquals(bookingDtoForUpdateAndGet.getId(), bookings.get(0).getId());
        Assertions.assertEquals(start, bookings.get(0).getStart());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getEnd(), bookings.get(0).getEnd());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getItem(), bookings.get(0).getItem());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getBooker(), bookings.get(0).getBooker());
        Assertions.assertEquals(bookingDtoForUpdateAndGet.getStatus(), bookings.get(0).getStatus());

        Mockito.verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemsCurrent(List.of(item), LocalDateTime.now());
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );
    }

    @Test
    void shouldCheckCorrectItemBookerAndBookingEndedNotStarted() {
        Mockito
                .when(bookingRepository.findAllByItemIdAndBookerId(1L, 2L))
                .thenReturn(List.of(booking));

        final IncorrectFieldException exception1 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> bookingService.checkCorrectItemBookerAndBookingEnded(2L, 1L));
        Assertions.assertEquals("The booking of the item has not yet started",exception1.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndBookerId(1L, 2L);
        Mockito.verifyNoMoreInteractions(
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
        Assertions.assertEquals("Incorrect item booker ID is specified",exception2.getMessage());

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(
                bookingRepository,
                itemRepository,
                userRepository,
                userService,
                itemService
        );
    }
}
