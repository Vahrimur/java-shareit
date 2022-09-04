package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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
    public List<BookingDtoForUpdateAndGet> getAllBookingsByBookerId(Long bookerId, String state)
            throws IncorrectObjectException, IncorrectEnumException {
        userService.checkUserExist(bookerId);
        checkState(state);
        State state1 = State.valueOf(state);
        List<Booking> bookings = new ArrayList<>();
        switch (state1) {
            case ALL:
                bookings = bookingRepository.findAllByBooker_Id(bookerId);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, BookingStatus.REJECTED);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBooker_IdCurrent(bookerId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBooker_IdFuture(bookerId, LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findAllByBooker_IdPast(bookerId, LocalDateTime.now());
                break;
        }
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookings);
    }

    @Override
    public List<BookingDtoForUpdateAndGet> getAllBookingsByOwnerId(Long ownerId, String state)
            throws IncorrectObjectException, IncorrectEnumException {
        userService.checkUserExist(ownerId);
        checkState(state);
        State state1 = State.valueOf(state);
        List<Booking> bookings = new ArrayList<>();
        List<Item> itemsByOwnerId = itemRepository.findAllByOwnerId(ownerId);
        switch (state1) {
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
        return BookingForUpdateAndGetMapper.mapToBookingDto(bookings);
    }

    private void checkState(String state) throws IncorrectEnumException {
        if (Arrays.stream(State.values()).noneMatch((st) -> st.name().equals(state))) {
            throw new IncorrectEnumException("Unknown state: " + state);
        }
    }


    private void checkCorrectTime(Booking booking) throws IncorrectFieldException {
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("Конец аренды не может быть в прошлом");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new IncorrectFieldException("Конец аренды не может быть раньше начала");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new IncorrectFieldException("Начало аренды не может быть в прошлом");
        }
    }

    private void checkBookingExist(Long bookingId) throws IncorrectObjectException {
        if (!bookingRepository.findAll().isEmpty()) {
            List<Long> ids = bookingRepository.findAll()
                    .stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(bookingId)) {
                throw new IncorrectObjectException("Введён некорректный id бронирования");
            }
        } else {
            throw new IncorrectObjectException("Введён некорректный id бронирования");
        }
    }

    private void checkSameStatus(Booking booking, boolean approved) throws IncorrectFieldException {
        if (booking.getStatus().equals(BookingStatus.APPROVED) && approved
                || booking.getStatus().equals(BookingStatus.REJECTED) && !approved) {
            throw new IncorrectFieldException("Невозможно обновить статус на тот же самый");
        }
    }

    private void checkCorrectItemOwnerOrBooker(Item item, Booking booking, Long userId)
            throws IncorrectObjectException {
        if (!booking.getBooker().getId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new IncorrectObjectException("Указан некорректный id арендатора или владельца вещи");
        }
    }

    @Override
    public void checkCorrectItemBookerAndBookingEnded(Long userId, Long itemId) throws IncorrectFieldException {
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, userId);
        if (bookings.stream().noneMatch(b -> b.getStart().isBefore(LocalDateTime.now()))) {
            throw new IncorrectFieldException("Срок аренды вещи еще не началсяся");
        }
        if (bookings.stream().noneMatch(b -> b.getBooker().getId().equals(userId))) {
            throw new IncorrectFieldException("Указан некорректный id арендатора вещи");
        }
    }
}
