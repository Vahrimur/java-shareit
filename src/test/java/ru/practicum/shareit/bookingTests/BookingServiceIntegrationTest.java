package ru.practicum.shareit.bookingTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForUpdateAndGetMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final Item item = new Item(
            null,
            "Дрель",
            "Простая дрель",
            true,
            1L,
            null
    );
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final UserDto userDto = UserMapper.mapToUserDto(new User(null, "user", "user@user.com"));
    private final UserDto bookerDto = UserMapper.mapToUserDto(new User(null, "name2", "email@email.ru"));
    private final BookingDto bookingDto = new BookingDto(
            null,
            LocalDateTime.now().plusSeconds(1),
            LocalDateTime.now().plusSeconds(2),
            1L,
            null,
            null,
            null
    );

    @Test
    void shouldAddNewBooking() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingCreated = query.setParameter("id", 1L).getSingleResult();

        Item itemCheck = ItemForGetMapper.mapToItemEntity(itemService.getItemById(1L, 1L), 1L);
        User bookerCheck = UserMapper.mapToUserEntity(userService.getUserById(2L));

        assertThat(bookingCreated.getId(), notNullValue());
        assertThat(bookingCreated.getId(), equalTo(1L));
        assertThat(bookingCreated.getStart(),
                anyOf(greaterThan(LocalDateTime.now()), equalTo(LocalDateTime.now().plusSeconds(2))));
        assertThat(bookingCreated.getEnd(),
                anyOf(greaterThan(LocalDateTime.now().plusSeconds(1)), equalTo(LocalDateTime.now().plusSeconds(3))));
        assertThat(bookingCreated.getItem(), equalTo(itemCheck));
        assertThat(bookingCreated.getBooker(), equalTo(bookerCheck));
        assertThat(bookingCreated.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void shouldChangeBookingStatus() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        bookingService.changeBookingStatus(1L, 1L, true);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingUpdated = query.setParameter("id", 1L).getSingleResult();

        Item itemCheck = ItemForGetMapper.mapToItemEntity(itemService.getItemById(1L, 1L), 1L);
        User bookerCheck = UserMapper.mapToUserEntity(userService.getUserById(2L));

        assertThat(bookingUpdated.getId(), notNullValue());
        assertThat(bookingUpdated.getId(), equalTo(1L));
        assertThat(bookingUpdated.getStart(),
                anyOf(greaterThan(LocalDateTime.now()), equalTo(LocalDateTime.now().plusSeconds(2))));
        assertThat(bookingUpdated.getEnd(),
                anyOf(greaterThan(LocalDateTime.now().plusSeconds(1)), equalTo(LocalDateTime.now().plusSeconds(3))));
        assertThat(bookingUpdated.getItem(), equalTo(itemCheck));
        assertThat(bookingUpdated.getBooker(), equalTo(bookerCheck));
        assertThat(bookingUpdated.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking bookingGet = query.setParameter("id", 1L).getSingleResult();

        Booking testBooking = BookingForUpdateAndGetMapper.mapToBookingEntity(
                bookingService.getBookingById(1L, 1L), 1L);

        assertThat(testBooking, equalTo(bookingGet));
    }

    @Test
    void shouldGetAllBookingsByBookerId() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b", Booking.class);
        List<Booking> bookings = query.getResultList();

        List<Booking> testBookings = BookingForUpdateAndGetMapper.mapToBookingEntity(
                bookingService.getAllBookingsByBookerId(2L, State.WAITING.toString()), 1L);

        assertThat(testBookings, equalTo(bookings));
    }

    @Test
    void shouldGetAllBookingsByBookerIdByPages() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b", Booking.class);
        List<Booking> bookings = query.getResultList();

        List<Booking> testBookings = BookingForUpdateAndGetMapper.mapToBookingEntity(
                bookingService.getAllBookingsByBookerIdByPages(
                        2L, State.WAITING.toString(), 1, 2), 1L);

        assertThat(testBookings, equalTo(bookings));
    }

    @Test
    void shouldGetAllBookingsByOwnerId() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b", Booking.class);
        List<Booking> bookings = query.getResultList();

        List<Booking> testBookings = BookingForUpdateAndGetMapper.mapToBookingEntity(
                bookingService.getAllBookingsByOwnerId(1L, State.WAITING.toString()), 1L);

        assertThat(testBookings, equalTo(bookings));
    }

    @Test
    void shouldGetAllBookingsByOwnerIdByPages() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b", Booking.class);
        List<Booking> bookings = query.getResultList();

        List<Booking> testBookings = BookingForUpdateAndGetMapper.mapToBookingEntity(
                bookingService.getAllBookingsByOwnerIdByPages(
                        1L, State.WAITING.toString(), 1, 2), 1L);

        assertThat(testBookings, equalTo(bookings));
    }
}
