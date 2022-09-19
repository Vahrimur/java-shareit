package ru.practicum.shareit.requestTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;

    private final ItemRequestService itemRequestService;

    private final User requester = new User(null, "user", "user@user.com");
    private final User asker = new User(null, "user2", "user2@user.com");
    private final UserDto requesterDto = UserMapper.mapToUserDto(requester);
    private final UserDto askerDto = UserMapper.mapToUserDto(asker);
    private final Item item = new Item(
            null, "Дрель", "Простая дрель", true, null, null);
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final ItemRequest itemRequest = new ItemRequest(
            null,
            "Хотел бы воспользоваться дрелью",
            null,
            null
    );

    private final ItemRequestDto itemRequestDto = ItemRequestMapper.mapToItemRequestDto(itemRequest);

    @Test
    void shouldAddNewItemRequest() throws Exception {
        userService.createUser(requesterDto);
        itemService.addNewItem(1L, itemDto);
        itemRequestService.addNewItemRequest(1L, itemRequestDto);

        TypedQuery<ItemRequest> query = em.createQuery("" +
                "Select i from ItemRequest i where i.description = :description", ItemRequest.class);
        ItemRequest itemRequestCreated = query.setParameter(
                "description", itemRequestDto.getDescription()).getSingleResult();

        User requesterGet = UserMapper.mapToUserEntity(userService.getUserById(1L));

        assertThat(itemRequestCreated.getId(), notNullValue());
        assertThat(itemRequestCreated.getId(), equalTo(1L));
        assertThat(itemRequestCreated.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestCreated.getRequester(), equalTo(requesterGet));
        assertThat(itemRequestCreated.getCreated(),
                anyOf(greaterThan(LocalDateTime.now().minusMinutes(1)), equalTo(LocalDateTime.now().plusMinutes(1))));
    }

    @Test
    void shouldGetAllItemRequestsByRequesterId() throws Exception {
        userService.createUser(requesterDto);
        itemService.addNewItem(1L, itemDto);
        itemRequestService.addNewItemRequest(1L, itemRequestDto);

        User requesterGet = UserMapper.mapToUserEntity(userService.getUserById(1L));

        TypedQuery<ItemRequest> query = em.createQuery("Select i from ItemRequest i", ItemRequest.class);
        List<ItemRequest> itemRequests = query.getResultList();

        List<ItemRequest> testRequests = ItemRequestForGetMapper.mapToItemRequestEntity(
                itemRequestService.getAllItemRequestsByRequesterId(1L), requesterGet);

        assertThat(testRequests, equalTo(itemRequests));
    }

    @Test
    void shouldGetAllItemRequests() throws Exception {
        userService.createUser(requesterDto);
        userService.createUser(askerDto);
        itemService.addNewItem(1L, itemDto);
        itemRequestService.addNewItemRequest(1L, itemRequestDto);

        User requesterGet = UserMapper.mapToUserEntity(userService.getUserById(1L));

        TypedQuery<ItemRequest> query = em.createQuery("Select i from ItemRequest i", ItemRequest.class);
        List<ItemRequest> itemRequests = query.getResultList();

        List<ItemRequest> testRequests = ItemRequestForGetMapper.mapToItemRequestEntity(
                itemRequestService.getAllItemRequests(2L), requesterGet);

        assertThat(testRequests, equalTo(itemRequests));
    }

    @Test
    void shouldGetAllItemRequestsByPages() throws Exception {
        userService.createUser(requesterDto);
        userService.createUser(askerDto);
        itemService.addNewItem(1L, itemDto);
        itemRequestService.addNewItemRequest(1L, itemRequestDto);

        User requesterGet = UserMapper.mapToUserEntity(userService.getUserById(1L));

        TypedQuery<ItemRequest> query = em.createQuery("Select i from ItemRequest i", ItemRequest.class);
        List<ItemRequest> itemRequests = query.getResultList();

        List<ItemRequest> testRequests = ItemRequestForGetMapper.mapToItemRequestEntity(
                itemRequestService.getAllItemRequestsByPages(2L, 1, 2), requesterGet);

        assertThat(testRequests, equalTo(itemRequests));
    }

    @Test
    void shouldGetItemRequestById() throws Exception {
        userService.createUser(requesterDto);
        itemService.addNewItem(1L, itemDto);
        itemRequestService.addNewItemRequest(1L, itemRequestDto);

        User requesterGet = UserMapper.mapToUserEntity(userService.getUserById(1L));

        TypedQuery<ItemRequest> query = em.createQuery("" +
                "Select i from ItemRequest i where i.description = :description", ItemRequest.class);
        ItemRequest itemRequestGot = query.setParameter(
                "description", itemRequestDto.getDescription()).getSingleResult();

        ItemRequest testItemRequest = ItemRequestForGetMapper.mapToItemRequestEntity(
                itemRequestService.getItemRequestById(1L, 1L), requesterGet);

        assertThat(testItemRequest, equalTo(itemRequestGot));
    }
}
