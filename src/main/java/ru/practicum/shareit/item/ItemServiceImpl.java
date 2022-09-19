package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectObjectException, IncorrectFieldException {
        Item item = ItemMapper.mapToItemEntity(itemDto, userId);
        userService.checkUserExist(userId);
        checkCorrectItem(item);
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
    public List<ItemDtoForGet> getAllItemsByUserId(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDtoForGet> itemDtos = new ArrayList<>();
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
        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        List<Item> items = new ArrayList<>();
        if (!text.equals("")) {
            items = itemRepository.searchByText(text);
        }
        return ItemMapper.mapToItemDto(items);
    }

    @Override
    public CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto)
            throws IncorrectObjectException, IncorrectFieldException {
        userService.checkUserExist(userId);
        checkItemExist(itemId);
        checkTextExists(commentDto);
        bookingService.checkCorrectItemBookerAndBookingEnded(userId, itemId);
        Item item = itemRepository.getById(itemId);
        User author = UserMapper.mapToUserEntity(userService.getUserById(userId));
        Comment comment = CommentMapper.mapToCommentEntity(commentDto, item, author);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<ItemDtoForGet> getAllItemsByUserIdByPages(Long userId, Integer from, Integer size) throws IncorrectObjectException {
        userService.checkUserExist(userId);
        checkPageableParams(from, size);
        Pageable sorted = PageRequest.of((from / size), size);
        List<Item> items = itemRepository.findAllByOwnerIdByPages(userId, sorted);
        List<ItemDtoForGet> itemDtos = new ArrayList<>();
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
        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItemsByTextByPages(String text, Integer from, Integer size) {
        checkPageableParams(from, size);
        Pageable sorted = PageRequest.of((from / size), size);
        List<Item> items = new ArrayList<>();
        if (!text.equals("")) {
            items = itemRepository.searchByTextByPages(text, sorted);
        }
        return ItemMapper.mapToItemDto(items);
    }

    private void checkPageableParams(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Index of start element cannot be less zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal zero");
        }
    }

    private void checkCorrectItem(Item item) throws IncorrectFieldException {
        if (item.getAvailable() == null || !item.getAvailable()) {
            throw new IncorrectFieldException("The item must be available for booking");
        }
        if (item.getName() == null || item.getName().equals("")) {
            throw new IncorrectFieldException("The name of the item cannot be empty");
        }
        if (item.getDescription() == null) {
            throw new IncorrectFieldException("The description of the item cannot be empty");
        }
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


    private void checkTextExists(CommentDto commentDto) throws IncorrectFieldException {
        if (!StringUtils.hasText(commentDto.getText())) {
            throw new IncorrectFieldException("The comment text cannot be empty");
        }
    }

    private BookingDto findLastBooking(Long itemId) {
        Booking lastBooking = bookingRepository.findAllByItemIdAndEndBeforeNow(
                itemId, LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30)
        )
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
        Booking nextBooking = bookingRepository.findAllByItemIdAndStartAfterNow(itemId, LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30)
        )
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
