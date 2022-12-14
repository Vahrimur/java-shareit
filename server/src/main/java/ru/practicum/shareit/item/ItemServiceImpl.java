package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    @Lazy
    private final BookingService bookingService;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectObjectException {
        Item item = ItemMapper.mapToItemEntity(itemDto, userId);
        userService.checkUserExist(userId);
        item.setOwnerId(userId);
        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId)
            throws IncorrectObjectException {
        Item item = ItemMapper.mapToItemEntity(itemDto, userId);
        item.setId(itemId);
        userService.checkUserExist(userId);
        checkItemExist(item.getId());
        checkCorrectItemOwner(item.getId(), userId);
        item.setOwnerId(userId);
        if (item.getName() == null) {
            item.setName(itemRepository.getById(item.getId()).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(itemRepository.getById(item.getId()).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(itemRepository.getById(item.getId()).getAvailable());
        }
        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDtoForGet getItemById(Long itemId, Long userId)
            throws IncorrectObjectException {
        checkItemExist(itemId);
        userService.checkUserExist(userId);
        BookingDto lastBookingDto;
        BookingDto nextBookingDto;
        if (Objects.equals(itemRepository.getReferenceById(itemId).getOwnerId(), userId)) {
            lastBookingDto = findLastBooking(itemId);
            nextBookingDto = findNextBooking(itemId);
        } else {
            lastBookingDto = null;
            nextBookingDto = null;
        }
        List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findAllByItemId(itemId));
        return ItemForGetMapper.mapToItemDto(itemRepository.getById(itemId),
                lastBookingDto,
                nextBookingDto,
                comments);
    }

    @Override
    public List<ItemDtoForGet> getAllItemsByUserId(Long userId, Integer from, Integer size)
            throws IncorrectObjectException {
        userService.checkUserExist(userId);
        List<ItemDtoForGet> itemDtos = new ArrayList<>();

        if (from == null || size == null) {
            List<Item> items = itemRepository.findAllByOwnerId(userId);
            if (items.isEmpty()) {
                return new ArrayList<>();
            }
            for (Item item : items) {
                Long itemId = item.getId();
                BookingDto lastBookingDto = null;
                BookingDto nextBookingDto = null;
                if (Objects.equals(item.getOwnerId(), userId)) {
                    lastBookingDto = findLastBooking(itemId);
                    nextBookingDto = findNextBooking(itemId);
                }
                List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findAllByItemId(itemId));
                itemDtos.add(ItemForGetMapper.mapToItemDto(item, lastBookingDto, nextBookingDto, comments));
            }
        } else {
            Pageable sorted = PageRequest.of((from / size), size);
            List<Item> items = itemRepository.findAllByOwnerIdByPages(userId, sorted);

            if (items.isEmpty()) {
                return new ArrayList<>();
            }
            for (Item item : items) {
                Long itemId = item.getId();
                BookingDto lastBookingDto = null;
                BookingDto nextBookingDto = null;
                if (Objects.equals(item.getOwnerId(), userId)) {
                    lastBookingDto = findLastBooking(itemId);
                    nextBookingDto = findNextBooking(itemId);
                }
                List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findAllByItemId(itemId));
                itemDtos.add(ItemForGetMapper.mapToItemDto(item, lastBookingDto, nextBookingDto, comments));
            }
        }
        List<ItemDtoForGet> list1 = itemDtos.stream()
                .filter(i -> i.getLastBooking() != null)
                .sorted(Comparator.nullsLast(Comparator.comparing(i -> i.getLastBooking().getStart())))
                .collect(Collectors.toList());
        List<ItemDtoForGet> list2 = itemDtos.stream()
                .filter(i -> i.getLastBooking() == null)
                .collect(Collectors.toList());
        return Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemsByText(String text, Integer from, Integer size, Long userId)
            throws IncorrectObjectException {
        userService.checkUserExist(userId);
        List<Item> items = new ArrayList<>();
        if (!"".equals(text)) {
            if (from == null || size == null) {
                items = itemRepository.searchByText(text);
            } else {
                Pageable sorted = PageRequest.of((from / size), size);
                items = itemRepository.searchByTextByPages(text, sorted);
            }
        }
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto)
            throws IncorrectObjectException, IncorrectFieldException {
        userService.checkUserExist(userId);
        checkItemExist(itemId);
        bookingService.checkCorrectItemBookerAndBookingEnded(userId, itemId);
        Item item = itemRepository.getById(itemId);
        User author = UserMapper.mapToUserEntity(userService.getUserById(userId));
        Comment comment = CommentMapper.mapToCommentEntity(commentDto, item, author);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public void checkItemExist(Long id) throws IncorrectObjectException {
        if (!itemRepository.findAll().isEmpty()) {
            List<Long> ids = itemRepository.findAll().stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(id)) {
                throw new IncorrectObjectException("There is no item with such ID");
            }
        } else {
            throw new IncorrectObjectException("There is no item with such ID");
        }
    }

    @Override
    public void checkItemAvailable(Long id) throws IncorrectFieldException {
        Item item = itemRepository.findById(id).get();
        if (!item.getAvailable()) {
            throw new IncorrectFieldException("The item is not available for booking");
        }
    }

    @Override
    public void checkCorrectItemOwner(Long itemId, Long userId) throws IncorrectObjectException {
        if (!Objects.equals(itemRepository.getById(itemId).getOwnerId(), userId)) {
            throw new IncorrectObjectException("Incorrect item owner ID is specified");
        }
    }

    @Override
    public void checkItemOwner(Long itemId, Long userId) throws IncorrectObjectException {
        if (Objects.equals(itemRepository.getById(itemId).getOwnerId(), userId)) {
            throw new IncorrectObjectException("This user is the owner of the item");
        }
    }

    private BookingDto findLastBooking(Long itemId) {
        Booking lastBooking = bookingRepository.findAllByItemIdAndEndBeforeNow(itemId, LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);
        BookingDto lastBookingDto = null;
        if (lastBooking != null) {
            lastBookingDto = BookingMapper.mapToBookingDto(lastBooking);
        }
        return lastBookingDto;
    }

    private BookingDto findNextBooking(Long itemId) {
        Booking nextBooking = bookingRepository.findAllByItemIdAndStartAfterNow(itemId, LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);
        BookingDto nextBookingDto = null;
        if (nextBooking != null) {
            nextBookingDto = BookingMapper.mapToBookingDto(nextBooking);
        }
        return nextBookingDto;
    }
}
