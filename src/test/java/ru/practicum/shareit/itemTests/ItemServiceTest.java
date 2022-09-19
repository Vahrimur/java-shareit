package ru.practicum.shareit.itemTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForGet;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserService userService;
    @Lazy
    @Mock
    BookingService bookingService;
    ItemService itemService;

    private MockitoSession session;

    private final Item item = new Item(
            1L,
            "Дрель",
            "Простая дрель",
            true,
            1L,
            null
    );
    private final ItemDto itemDto = new ItemDto(
            1L,
            "Дрель",
            "Простая дрель",
            true,
            null
    );
    private final CommentDto commentDto = new CommentDto(
            2L,
            "Add comment from user2",
            "name",
            LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30)
    );
    private final List<CommentDto> comments = List.of(commentDto);
    private final ItemDtoForGet itemDtoForGet = new ItemDtoForGet(
            2L,
            "Дрель",
            "Простая дрель",
            true,
            null,
            null,
            comments,
            null
    );
    private final Item itemForGet = new Item(
            2L,
            "Дрель",
            "Простая дрель",
            true,
            1L,
            null
    );
    private final User booker = new User(2L, "name", "email@email.ru");
    private final UserDto bookerDto = UserMapper.mapToUserDto(booker);
    private final Booking booking = new Booking(
            2L,
            LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30),
            LocalDateTime.of(2022, Month.SEPTEMBER, 9, 12, 30, 30),
            itemForGet,
            booker,
            BookingStatus.WAITING
    );
    private final BookingDto bookingDto = BookingMapper.mapToBookingDto(booking);
    private final List<Comment> commentsEntity = List.of(new Comment(
            2L,
            "Add comment from user2",
            itemForGet,
            booker,
            LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30)
    ));
    private final Comment comment = CommentMapper.mapToCommentEntity(commentDto, itemForGet, booker);

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        itemService = new ItemServiceImpl(
                itemRepository,
                bookingRepository,
                commentRepository,
                userService,
                bookingService
        );
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    void shouldAddNewItem() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto newItem = itemService.addNewItem(1L, itemDto);

        Assertions.assertEquals(itemDto.getId(), newItem.getId());
        Assertions.assertEquals(itemDto.getName(), newItem.getName());
        Assertions.assertEquals(itemDto.getDescription(), newItem.getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), newItem.getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), newItem.getRequestId());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .save(item);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );

        itemDto.setAvailable(false);
        final IncorrectFieldException exception1 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));
        Assertions.assertEquals("The item must be available for booking", exception1.getMessage());

        itemDto.setAvailable(true);
        itemDto.setName("");
        final IncorrectFieldException exception2 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));
        Assertions.assertEquals("The name of the item cannot be empty", exception2.getMessage());

        itemDto.setName("name");
        itemDto.setDescription(null);
        final IncorrectFieldException exception3 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));
        Assertions.assertEquals("The description of the item cannot be empty", exception3.getMessage());
    }

    @Test
    void shouldUpdateItem() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(item));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto updatedItem = itemService.updateItem(1L, itemDto, 1L);

        Assertions.assertEquals(itemDto.getId(), updatedItem.getId());
        Assertions.assertEquals(itemDto.getName(), updatedItem.getName());
        Assertions.assertEquals(itemDto.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), updatedItem.getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), updatedItem.getRequestId());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .save(item);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldUpdateItemNullFields() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(item));
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);
        Mockito
                .when(itemRepository.save(any()))
                .thenReturn(item);

        itemDto.setName(null);
        itemDto.setDescription(null);
        itemDto.setAvailable(null);

        ItemDto updatedItem = itemService.updateItem(1L, itemDto, 1L);

        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);

        Assertions.assertEquals(itemDto.getId(), updatedItem.getId());
        Assertions.assertEquals(itemDto.getName(), updatedItem.getName());
        Assertions.assertEquals(itemDto.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), updatedItem.getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), updatedItem.getRequestId());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(4))
                .getById(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .save(item);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetItemByIdByOwner() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(itemForGet));
        Mockito
                .when(itemRepository.getReferenceById(2L))
                .thenReturn(itemForGet);
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndEndBeforeNow(anyLong(), any()))
                .thenReturn(List.of(booking));
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndStartAfterNow(anyLong(), any()))
                .thenReturn(new ArrayList<>());
        Mockito
                .when(commentRepository.findAllByItemId(2L))
                .thenReturn(commentsEntity);
        Mockito
                .when(itemRepository.getById(2L))
                .thenReturn(itemForGet);

        ItemDtoForGet itemDtoGot = itemService.getItemById(2L, 1L);

        Assertions.assertEquals(itemDtoForGet.getId(), itemDtoGot.getId());
        Assertions.assertEquals(itemDtoForGet.getName(), itemDtoGot.getName());
        Assertions.assertEquals(itemDtoForGet.getDescription(), itemDtoGot.getDescription());
        Assertions.assertEquals(itemDtoForGet.getAvailable(), itemDtoGot.getAvailable());
        Assertions.assertEquals(bookingDto, itemDtoGot.getLastBooking());
        Assertions.assertEquals(itemDtoForGet.getNextBooking(), itemDtoGot.getNextBooking());
        Assertions.assertEquals(itemDtoForGet.getComments(), itemDtoGot.getComments());
        Assertions.assertEquals(itemDtoForGet.getRequestId(), itemDtoGot.getRequestId());

        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getReferenceById(2L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndEndBeforeNow(anyLong(), any());
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndStartAfterNow(anyLong(), any());
        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(commentRepository, Mockito.times(1))
                .findAllByItemId(2L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(2L);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetItemByIdByBooker() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(itemForGet));
        Mockito
                .when(itemRepository.getReferenceById(2L))
                .thenReturn(itemForGet);
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndEndBeforeNow(2L,
                        LocalDateTime.now()))
                .thenReturn(List.of(booking));
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndStartAfterNow(2L,
                        LocalDateTime.now()))
                .thenReturn(new ArrayList<>());
        Mockito
                .when(commentRepository.findAllByItemId(2L))
                .thenReturn(commentsEntity);
        Mockito
                .when(itemRepository.getById(2L))
                .thenReturn(itemForGet);

        ItemDtoForGet itemDtoGot = itemService.getItemById(2L, 2L);

        Assertions.assertEquals(itemDtoForGet.getId(), itemDtoGot.getId());
        Assertions.assertEquals(itemDtoForGet.getName(), itemDtoGot.getName());
        Assertions.assertEquals(itemDtoForGet.getDescription(), itemDtoGot.getDescription());
        Assertions.assertEquals(itemDtoForGet.getAvailable(), itemDtoGot.getAvailable());
        Assertions.assertNull(itemDtoGot.getLastBooking());
        Assertions.assertNull(itemDtoGot.getNextBooking());
        Assertions.assertEquals(itemDtoForGet.getComments(), itemDtoGot.getComments());
        Assertions.assertEquals(itemDtoForGet.getRequestId(), itemDtoGot.getRequestId());

        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getReferenceById(2L);
        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(commentRepository, Mockito.times(1))
                .findAllByItemId(2L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(2L);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetAllItemsByUserId() {
        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(List.of(itemForGet));
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndEndBeforeNow(anyLong(), any()))
                .thenReturn(List.of(booking));
        Mockito
                .lenient()
                .when(bookingRepository.findAllByItemIdAndStartAfterNow(anyLong(), any()))
                .thenReturn(new ArrayList<>());
        Mockito
                .when(commentRepository.findAllByItemId(2L))
                .thenReturn(commentsEntity);

        List<ItemDtoForGet> items = itemService.getAllItemsByUserId(1L);

        Assertions.assertEquals(itemDtoForGet.getId(), items.get(0).getId());
        Assertions.assertEquals(itemDtoForGet.getName(), items.get(0).getName());
        Assertions.assertEquals(itemDtoForGet.getDescription(), items.get(0).getDescription());
        Assertions.assertEquals(itemDtoForGet.getAvailable(), items.get(0).getAvailable());
        Assertions.assertEquals(bookingDto, items.get(0).getLastBooking());
        Assertions.assertEquals(itemDtoForGet.getNextBooking(), items.get(0).getNextBooking());
        Assertions.assertEquals(itemDtoForGet.getComments(), items.get(0).getComments());
        Assertions.assertEquals(itemDtoForGet.getRequestId(), items.get(0).getRequestId());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndEndBeforeNow(anyLong(), any());
        Mockito
                .verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdAndStartAfterNow(anyLong(), any());
        Mockito
                .verify(commentRepository, Mockito.times(1))
                .findAllByItemId(2L);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetAllItemsByUserIdEmpty() {
        Mockito
                .when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(new ArrayList<>());

        List<ItemDtoForGet> items = itemService.getAllItemsByUserId(1L);

        Assertions.assertEquals(0, items.size());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerId(1L);
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldSearchItemsByText() {
        Mockito
                .when(itemRepository.searchByText(anyString()))
                .thenReturn(List.of(item));

        List<ItemDto> items = itemService.searchItemsByText("text");

        Assertions.assertEquals(itemDto.getId(), items.get(0).getId());
        Assertions.assertEquals(itemDto.getName(), items.get(0).getName());
        Assertions.assertEquals(itemDto.getDescription(), items.get(0).getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), items.get(0).getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), items.get(0).getRequestId());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .searchByText(anyString());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldSearchItemsByNullText() {
        List<ItemDto> items = itemService.searchItemsByText("");

        Assertions.assertEquals(0, items.size());

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .searchByText(anyString());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldAddNewComment() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(itemForGet));
        Mockito
                .when(itemRepository.getById(2L))
                .thenReturn(itemForGet);
        Mockito
                .when(userService.getUserById(2L))
                .thenReturn(bookerDto);
        Mockito
                .when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDto newCommentDto = itemService.addNewComment(2L, 2L, commentDto);

        Assertions.assertEquals(commentDto.getId(), newCommentDto.getId());
        Assertions.assertEquals(commentDto.getText(), newCommentDto.getText());
        Assertions.assertEquals(commentDto.getAuthorName(), newCommentDto.getAuthorName());
        Assertions.assertNotEquals(LocalDateTime.now(), newCommentDto.getCreated());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(2L);
        Mockito
                .verify(bookingService, Mockito.times(1))
                .checkCorrectItemBookerAndBookingEnded(2L, 2L);
        Mockito
                .verify(userService, Mockito.times(1))
                .getUserById(2L);
        Mockito
                .verify(commentRepository, Mockito.times(1))
                .save(any());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetAllItemsByUserIdByPages() throws IncorrectObjectException {
        Mockito
                .when(itemRepository.findAllByOwnerIdByPages(anyLong(), any()))
                .thenReturn(List.of(item));
        Mockito
                .when(commentRepository.findAllByItemId(1L))
                .thenReturn(new ArrayList<>());

        List<ItemDtoForGet> items = itemService.getAllItemsByUserIdByPages(2L, 1, 2);

        Assertions.assertEquals(itemDto.getId(), items.get(0).getId());
        Assertions.assertEquals(itemDto.getName(), items.get(0).getName());
        Assertions.assertEquals(itemDto.getDescription(), items.get(0).getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), items.get(0).getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), items.get(0).getRequestId());
        Assertions.assertNull(items.get(0).getLastBooking());
        Assertions.assertNull(items.get(0).getNextBooking());
        Assertions.assertEquals(new ArrayList<>(), items.get(0).getComments());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerIdByPages(anyLong(), any());
        Mockito
                .verify(bookingRepository, Mockito.times(0))
                .findAllByItemIdAndEndBeforeNow(anyLong(), any());
        Mockito
                .verify(bookingRepository, Mockito.times(0))
                .findAllByItemIdAndStartAfterNow(anyLong(), any());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldGetAllItemsByUserIdByPagesEmpty() throws IncorrectObjectException {
        Mockito
                .when(itemRepository.findAllByOwnerIdByPages(anyLong(), any()))
                .thenReturn(new ArrayList<>());

        List<ItemDtoForGet> items = itemService.getAllItemsByUserIdByPages(2L, 1, 2);

        Assertions.assertEquals(0, items.size());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(2L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByOwnerIdByPages(anyLong(), any());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldSearchItemsByTextByPages() {
        Mockito
                .when(itemRepository.searchByTextByPages(anyString(), any()))
                .thenReturn(List.of(item));

        List<ItemDto> items = itemService.searchItemsByTextByPages("text", 1, 1);

        Assertions.assertEquals(itemDto.getId(), items.get(0).getId());
        Assertions.assertEquals(itemDto.getName(), items.get(0).getName());
        Assertions.assertEquals(itemDto.getDescription(), items.get(0).getDescription());
        Assertions.assertEquals(itemDto.getAvailable(), items.get(0).getAvailable());
        Assertions.assertEquals(itemDto.getRequestId(), items.get(0).getRequestId());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .searchByTextByPages(anyString(), any());
        Mockito
                .verifyNoMoreInteractions(itemRepository,
                        bookingRepository,
                        commentRepository,
                        userService,
                        bookingService
                );
    }

    @Test
    void shouldSearchItemsByTextByPagesWrongParameters() {
        final IllegalArgumentException exception1 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItemsByTextByPages("text", -1, 1));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .searchByTextByPages(anyString(), any());
        Assertions.assertEquals("Index of start element cannot be less zero", exception1.getMessage());

        final IllegalArgumentException exception2 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> itemService.searchItemsByTextByPages("text", 1, 0));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .searchByTextByPages(anyString(), any());
        Assertions.assertEquals("Page size cannot be less or equal zero", exception2.getMessage());
    }

    @Test
    void shouldCheckCorrectItemFails() {
        itemDto.setAvailable(false);
        final IncorrectFieldException exception1 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .save(any());
        Assertions.assertEquals("The item must be available for booking", exception1.getMessage());

        itemDto.setAvailable(true);
        itemDto.setName("");
        final IncorrectFieldException exception2 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .save(any());
        Assertions.assertEquals("The name of the item cannot be empty", exception2.getMessage());

        itemDto.setName("Дрель");
        itemDto.setDescription(null);
        final IncorrectFieldException exception3 = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewItem(1L, itemDto));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .save(any());
        Assertions.assertEquals("The description of the item cannot be empty", exception3.getMessage());
    }

    @Test
    void shouldCheckItemExistFails() throws IncorrectObjectException, IncorrectFieldException {
        final IncorrectObjectException exception1 = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemService.checkItemExist(10L));
        Assertions.assertEquals("There is no item with such ID", exception1.getMessage());

        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(item));

        final IncorrectObjectException exception2 = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemService.checkItemExist(10L));
        Assertions.assertEquals("There is no item with such ID", exception2.getMessage());

        Mockito
                .verify(itemRepository, Mockito.times(3))
                .findAll();
    }

    @Test
    void shouldCheckCorrectItemOwner() {
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemService.checkCorrectItemOwner(1L, 10L));
        Assertions.assertEquals("Incorrect item owner ID is specified", exception.getMessage());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
    }

    @Test
    void shouldCheckItemOwner() {
        Mockito
                .when(itemRepository.getById(1L))
                .thenReturn(item);

        final IncorrectObjectException exception = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemService.checkItemOwner(1L, 1L));
        Assertions.assertEquals("This user is the owner of the item", exception.getMessage());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .getById(1L);
    }

    @Test
    void shouldCheckTextExists() {
        commentDto.setText(null);
        Mockito
                .when(itemRepository.findAll())
                .thenReturn(List.of(item));

        final IncorrectFieldException exception = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.addNewComment(1L, 1L, commentDto));
        Assertions.assertEquals("The comment text cannot be empty", exception.getMessage());

        Mockito
                .verify(itemRepository, Mockito.times(2))
                .findAll();
    }

    @Test
    void shouldCheckItemAvailable() {
        item.setAvailable(false);
        Mockito
                .when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        final IncorrectFieldException exception = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemService.checkItemAvailable(1L));
        Assertions.assertEquals("The item is not available for booking", exception.getMessage());

        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findById(1L);
    }
}
