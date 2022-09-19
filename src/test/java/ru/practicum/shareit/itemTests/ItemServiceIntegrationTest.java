package ru.practicum.shareit.itemTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final Item item = new Item(
            1L,
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
    private final CommentDto commentDto = new CommentDto(
            null,
            "Add comment from booker",
            null,
            null
    );

    @Test
    void shouldAddNewItem() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item itemCreated = query.setParameter("name", itemDto.getName()).getSingleResult();

        assertThat(itemCreated.getId(), notNullValue());
        assertThat(itemCreated.getId(), equalTo(1L));
        assertThat(itemCreated.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemCreated.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(itemCreated.getRequestId(), equalTo(itemDto.getRequestId()));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);
        itemDto.setDescription("Непростая дрель");
        itemService.updateItem(1L, itemDto, 1L);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item itemCreated = query.setParameter("name", itemDto.getName()).getSingleResult();

        assertThat(itemCreated.getId(), notNullValue());
        assertThat(itemCreated.getId(), equalTo(1L));
        assertThat(itemCreated.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemCreated.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(itemCreated.getRequestId(), equalTo(itemDto.getRequestId()));
    }

    @Test
    void shouldGetItemById() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item itemGet = query.setParameter("name", itemDto.getName()).getSingleResult();

        Item testItem = ItemForGetMapper.mapToItemEntity(itemService.getItemById(1L, 1L), 1L);

        assertThat(testItem, equalTo(itemGet));
    }

    @Test
    void shouldGetAllItemsByUserId() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i", Item.class);
        List<Item> items = query.getResultList();

        List<Item> testItems = ItemForGetMapper.mapToItemEntity(itemService.getAllItemsByUserId(1L), 1L);

        assertThat(testItems, equalTo(items));
    }

    @Test
    void shouldSearchItemsByText() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i", Item.class);
        List<Item> items = query.getResultList();

        List<Item> testItems = ItemMapper.mapToItemEntity(itemService.searchItemsByText("Дрель"), 1L);

        assertThat(testItems, equalTo(items));
    }

    @Test
    void shouldAddNewComment() throws Exception {
        userService.createUser(userDto);
        userService.createUser(bookerDto);
        itemService.addNewItem(1L, itemDto);
        bookingService.addNewBooking(2L, bookingDto);
        TimeUnit.SECONDS.sleep(1);
        itemService.addNewComment(2L, 1L, commentDto);

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c where c.text = :text", Comment.class);
        Comment commentCreated = query.setParameter("text", commentDto.getText()).getSingleResult();

        assertThat(commentCreated.getId(), notNullValue());
        assertThat(commentCreated.getId(), equalTo(1L));
        assertThat(commentCreated.getText(), equalTo(commentDto.getText()));

        User bookerCheck = UserMapper.mapToUserEntity(userService.getUserById(2L));
        Item itemCheck = ItemForGetMapper.mapToItemEntity(itemService.getItemById(1L, 1L), 1L);
        Comment commentCheck = CommentMapper.mapToCommentEntity(
                itemService.getItemById(1L, 1L).getComments().get(0),
                itemCheck,
                bookerCheck);

        assertThat(commentCreated, equalTo(commentCheck));
    }

    @Test
    void shouldGetAllItemsByUserIdByPages() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i", Item.class);
        List<Item> items = query.getResultList();

        List<Item> testItems = ItemForGetMapper.mapToItemEntity(
                itemService.getAllItemsByUserIdByPages(1L, 1, 2), 1L);

        assertThat(testItems, equalTo(items));
    }

    @Test
    void shouldSearchItemsByTextByPages() throws Exception {
        userService.createUser(userDto);
        itemService.addNewItem(1L, itemDto);

        TypedQuery<Item> query = em.createQuery("Select i from Item i", Item.class);
        List<Item> items = query.getResultList();

        List<Item> testItems = ItemMapper.mapToItemEntity(
                itemService.searchItemsByTextByPages("Дрель", 1, 2), 1L);

        assertThat(testItems, equalTo(items));
    }
}
