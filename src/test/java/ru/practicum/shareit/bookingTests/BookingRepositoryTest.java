package ru.practicum.shareit.bookingTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private final Item item = new Item(
            null,
            "Дрель",
            "Простая дрель",
            true,
            1L,
            null
    );
    private final User owner = new User(null, "user", "user@user.com");
    private final User booker = new User(null, "name2", "email@email.ru");
    Booking booking = new Booking(
            null,
            LocalDateTime.now().plusSeconds(1),
            LocalDateTime.now().plusSeconds(2),
            null,
            null,
            null
    );

    @BeforeEach
    void setUp() {
        userRepository.save(owner);
        userRepository.save(booker);
        itemRepository.save(item);
        booking.setBooker(userRepository.getById(2L));
        booking.setItem(itemRepository.getById(1L));
        bookingRepository.save(booking);
    }

    @Test
    void shouldFindAllByBookerId() {
        Assertions.assertEquals(1, itemRepository.findAll().size());

        Booking testBooking = bookingRepository.findAllByBookerId(2L).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        Booking testBooking = bookingRepository.findAllByBookerIdByPages(2L, sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdCurrent() {
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        Booking testBooking = bookingRepository.findAllByBookerIdCurrent(2L, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdCurrentByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        booking.setStart(LocalDateTime.now().minusMinutes(1));
        booking.setEnd(LocalDateTime.now().plusMinutes(1));
        Booking testBooking = bookingRepository.findAllByBookerIdCurrentByPages(
                2L, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdFuture() {
        booking.setStart(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByBookerIdFuture(2L, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdFutureByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        booking.setStart(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByBookerIdFutureByPages(
                2L, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdPast() {
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Booking testBooking = bookingRepository.findAllByBookerIdPast(2L, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdPastByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Booking testBooking = bookingRepository.findAllByBookerIdPastByPages(
                2L, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdAndStatus() {
        booking.setStatus(BookingStatus.WAITING);
        Booking testBooking = bookingRepository.findAllByBookerIdAndStatus(2L, BookingStatus.WAITING).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByBookerIdAndStatusByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        booking.setStatus(BookingStatus.WAITING);
        Booking testBooking = bookingRepository.findAllByBookerIdAndStatusByPages(
                2L, BookingStatus.WAITING, sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItems() {
        List<Item> items = itemRepository.findAll();
        Booking testBooking = bookingRepository.findAllByItems(items).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        List<Item> items = itemRepository.findAll();
        Booking testBooking = bookingRepository.findAllByItemsByPages(items, sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemIdAndBookerId() {
        Booking testBooking = bookingRepository.findAllByItemIdAndBookerId(1L, 2L).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsAndStatus() {
        booking.setStatus(BookingStatus.WAITING);
        List<Item> items = itemRepository.findAll();
        Booking testBooking = bookingRepository.findAllByItemsAndStatus(items, BookingStatus.WAITING).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsAndStatusByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        booking.setStatus(BookingStatus.WAITING);
        List<Item> items = itemRepository.findAll();
        Booking testBooking = bookingRepository
                .findAllByItemsAndStatusByPages(items, BookingStatus.WAITING, sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsCurrent() {
        List<Item> items = itemRepository.findAll();
        booking.setStart(LocalDateTime.now().minusMinutes(1));
        booking.setEnd(LocalDateTime.now().plusMinutes(1));
        Booking testBooking = bookingRepository.findAllByItemsCurrent(items, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsCurrentByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        List<Item> items = itemRepository.findAll();
        booking.setEnd(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByItemsCurrentByPages(items, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsFuture() {
        List<Item> items = itemRepository.findAll();
        booking.setStart(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByItemsFuture(items, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsFutureByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        List<Item> items = itemRepository.findAll();
        booking.setStart(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByItemsFutureByPages(items, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsPast() {
        List<Item> items = itemRepository.findAll();
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Booking testBooking = bookingRepository.findAllByItemsPast(items, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemsPastByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("start").descending());
        List<Item> items = itemRepository.findAll();
        booking.setEnd(LocalDateTime.now().minusDays(1));
        Booking testBooking = bookingRepository.findAllByItemsPastByPages(items, LocalDateTime.now(), sorted).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemIdAndEndBeforeNow() {
        booking.setStart(LocalDateTime.now().minusMinutes(2));
        booking.setEnd(LocalDateTime.now().minusMinutes(1));
        Booking testBooking = bookingRepository.findAllByItemIdAndEndBeforeNow(1L, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }

    @Test
    void shouldFindAllByItemIdAndStartAfterNow() {
        booking.setStart(LocalDateTime.now().plusDays(1));
        Booking testBooking = bookingRepository.findAllByItemIdAndStartAfterNow(1L, LocalDateTime.now()).get(0);

        Assertions.assertEquals(1, testBooking.getId());
        Assertions.assertEquals(booking.getStart(), testBooking.getStart());
        Assertions.assertEquals(booking.getEnd(), testBooking.getEnd());
        Assertions.assertEquals(booking.getItem(), testBooking.getItem());
        Assertions.assertEquals(booking.getBooker(), testBooking.getBooker());
        Assertions.assertEquals(booking.getStatus(), testBooking.getStatus());
    }
}
