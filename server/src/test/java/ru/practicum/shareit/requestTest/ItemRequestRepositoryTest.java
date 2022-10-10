package ru.practicum.shareit.requestTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private final Item item = new Item(
            null, "Дрель", "Простая дрель", true, 1L, null);
    private final User owner = new User(null, "user", "user@user.com");
    private final User requester = new User(null, "user2", "user2@user.com");
    private final ItemRequest itemRequest = new ItemRequest(
            null, "Хотел бы воспользоваться дрелью", requester, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        userRepository.save(owner);
        itemRepository.save(item);
        userRepository.save(requester);
    }

    @Test
    void shouldFindAll() {
        itemRequestRepository.save(itemRequest);

        Assertions.assertEquals(1, itemRequestRepository.findAll().size());
    }

    @Test
    void shouldFindAllAndGet() {
        itemRequestRepository.save(itemRequest);
        ItemRequest foundItem = itemRequestRepository.findAll().get(0);

        Assertions.assertEquals(1, foundItem.getId());
        Assertions.assertEquals(itemRequest.getDescription(), foundItem.getDescription());
        Assertions.assertEquals(itemRequest.getRequester(), foundItem.getRequester());
        Assertions.assertEquals(itemRequest.getCreated(), foundItem.getCreated());
    }

    @Test
    void shouldFindAllByPages() {
        Pageable sorted = PageRequest.of((1 / 2), 2, Sort.by("created").descending());

        itemRequestRepository.save(itemRequest);

        ItemRequest foundItem = itemRequestRepository.findAllByPages(1L, sorted).get(0);

        Assertions.assertEquals(1, itemRequestRepository.findAllByPages(1L, sorted).size());
        Assertions.assertEquals(1, foundItem.getId());
        Assertions.assertEquals(itemRequest.getDescription(), foundItem.getDescription());
        Assertions.assertEquals(itemRequest.getRequester(), foundItem.getRequester());
        Assertions.assertEquals(itemRequest.getCreated(), foundItem.getCreated());
    }
}
