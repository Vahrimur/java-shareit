package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForUpdateAndGet;
import ru.practicum.shareit.booking.dto.BookingForUpdateAndGetMapper;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.IncorrectEnumException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingDto addNewBooking(Long bookerId, BookingDto bookingDto)
            throws IncorrectObjectException, IncorrectFieldException {
        Item item1 = itemRepository.getById(bookingDto.getItemId());
        User booker = userRepository.getById(bookerId);
        Booking booking = BookingMapper.mapToBookingEntity(bookingDto, item1, booker);
        userService.checkUserExist(bookerId);
        itemService.checkItemExist(booking.getItem().getId());
        itemService.checkItemOwner(booking.getItem().getId(), bookerId);
        itemService.checkItemAvailable(booking.getItem().getId());
        checkCorrectTime(booking);
        booking.setBooker(userRepository.getById(bookerId));
        booking.setStatus(BookingStatus.WAITING);
        Item item = itemRepository.getById(booking.getItem().getId());
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoForUpdateAndGet changeBookingStatus(Long ownerId, Long bookingId, boolean approved)
            throws IncorrectObjectException, IncorrectFieldException {
        userService.checkUserExist(ownerId);
        checkBookingExist(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        Item item = itemRepository.getById(booking.getItem().getId());
        itemService.checkCorrectItemOwner(item.getId(), ownerId);
        checkSameStatus(booking, approved);
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoForUpdateAndGet getBookingById(Long bookingId, Long userId) throws IncorrectObjectException {
        userService.checkUserExist(userId);
        checkBookingExist(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        Item item = itemRepository.getById(booking.getItem().getId());
        checkCorrectItemOwnerOrBooker(item, booking, userId);
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookingRepository.getById(bookingId));
    }

    @Override
    public List<BookingDtoForUpdateAndGet> getAllBookingsByBookerId(
            Long bookerId, String state, Integer from, Integer size)
            throws IncorrectEnumException, IncorrectObjectException {
        userService.checkUserExist(bookerId);
        checkState(state);
        State bookingState = State.valueOf(state);
        List<Booking> bookings = new ArrayList<>();

        if (from == null || size == null) {
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerId(bookerId);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdCurrent(bookerId, LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdFuture(bookerId, LocalDateTime.now());
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdPast(bookerId, LocalDateTime.now());
                    break;
            }
        } else {
            checkPageableParams(from, size);
            Pageable sorted = PageRequest.of((from / size), size, Sort.by("start").descending());
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerIdByPages(bookerId, sorted);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatusByPages(bookerId,
                            BookingStatus.WAITING, sorted);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatusByPages(bookerId,
                            BookingStatus.REJECTED, sorted);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByBookerIdCurrentByPages(bookerId, LocalDateTime.now(), sorted);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByBookerIdFutureByPages(bookerId, LocalDateTime.now(), sorted);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByBookerIdPastByPages(bookerId, LocalDateTime.now(), sorted);
                    break;
            }
        }
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookings);
    }

    @Override
    public List<BookingDtoForUpdateAndGet> getAllBookingsByOwnerId(
            Long ownerId, String state, Integer from, Integer size)
            throws IncorrectObjectException, IncorrectEnumException {
        userService.checkUserExist(ownerId);
        checkState(state);
        State bookingState = State.valueOf(state);
        List<Booking> bookings = new ArrayList<>();
        List<Item> itemsByOwnerId = itemRepository.findAllByOwnerId(ownerId);

        if (from == null || size == null) {
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByItems(itemsByOwnerId);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByItemsAndStatus(itemsByOwnerId, BookingStatus.WAITING);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByItemsAndStatus(itemsByOwnerId, BookingStatus.REJECTED);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByItemsCurrent(itemsByOwnerId, LocalDateTime.now());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByItemsFuture(itemsByOwnerId, LocalDateTime.now());
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByItemsPast(itemsByOwnerId, LocalDateTime.now());
                    break;
            }
        } else {
            checkPageableParams(from, size);
            Pageable sorted = PageRequest.of((from / size), size, Sort.by("start").descending());
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByItemsByPages(itemsByOwnerId, sorted);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByItemsAndStatusByPages(itemsByOwnerId,
                            BookingStatus.WAITING, sorted);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByItemsAndStatusByPages(itemsByOwnerId,
                            BookingStatus.REJECTED, sorted);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findAllByItemsCurrentByPages(
                            itemsByOwnerId, LocalDateTime.now(), sorted);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findAllByItemsFutureByPages(
                            itemsByOwnerId, LocalDateTime.now(), sorted);
                    break;
                case PAST:
                    bookings = bookingRepository.findAllByItemsPastByPages(
                            itemsByOwnerId, LocalDateTime.now(), sorted);
                    break;
            }
        }
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookings);
    }

    private void checkPageableParams(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Index of start element cannot be less zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal zero");
        }
    }

    private void checkState(String state) throws IncorrectEnumException {
        if (Arrays.stream(State.values()).noneMatch((st) -> st.name().equals(state))) {
            throw new IncorrectEnumException("Unknown state: " + state);
        }
    }


    private void checkCorrectTime(Booking booking) throws IncorrectFieldException {
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("The end of the booking cannot be in the past");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectFieldException("The end of the booking cannot be earlier than the beginning");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("The beginning of the booking cannot be in the past");
        }
    }

    private void checkBookingExist(Long bookingId) throws IncorrectObjectException {
        if (!bookingRepository.findAll().isEmpty()) {
            List<Long> ids = bookingRepository.findAll()
                    .stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(bookingId)) {
                throw new IncorrectObjectException("There is no booking with such ID");
            }
        } else {
            throw new IncorrectObjectException("There is no booking with such ID");
        }
    }

    private void checkSameStatus(Booking booking, boolean approved) throws IncorrectFieldException {
        if (booking.getStatus().equals(BookingStatus.APPROVED) && approved
                || booking.getStatus().equals(BookingStatus.REJECTED) && !approved) {
            throw new IncorrectFieldException("It is not possible to update the booking status to the same");
        }
    }

    private void checkCorrectItemOwnerOrBooker(Item item, Booking booking, Long userId)
            throws IncorrectObjectException {
        if (!booking.getBooker().getId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new IncorrectObjectException("Incorrect item owner or booker ID is specified");
        }
    }

    @Override
    public void checkCorrectItemBookerAndBookingEnded(Long userId, Long itemId) throws IncorrectFieldException {
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, userId);
        if (bookings.stream().noneMatch(b -> b.getStart().isBefore(LocalDateTime.now()))) {
            throw new IncorrectFieldException("The booking of the item has not yet started");
        }
        if (bookings.stream().noneMatch(b -> b.getBooker().getId().equals(userId))) {
            throw new IncorrectFieldException("Incorrect item booker ID is specified");
        }
    }
}
