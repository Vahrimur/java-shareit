package ru.practicum.shareit.itemTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private final Item item = new Item(
            null, "Дрель", "Простая дрель", true, 1L, null);
    private final Item item2 = new Item(
            null, "Тарелка", "Простая тарелка", true, 1L, null);
    private final User owner = new User(null, "user", "user@user.com");

    @BeforeEach
    void setUp() {
        userRepository.save(owner);
    }

    @Test
    void shouldSearchByText() {
        Assertions.assertEquals(new ArrayList<>(), itemRepository.findAll());

        itemRepository.save(item);
        itemRepository.save(item2);

        Assertions.assertEquals(2, itemRepository.findAll().size());
        Assertions.assertEquals(1, itemRepository.searchByText("Дрель").size());

        Item itemByText = itemRepository.searchByText("Дрель").get(0);
        Assertions.assertEquals(1, itemByText.getId());
        Assertions.assertEquals(item.getName(), itemByText.getName());
        Assertions.assertEquals(item.getDescription(), itemByText.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemByText.getAvailable());
        Assertions.assertEquals(item.getOwnerId(), itemByText.getOwnerId());
        Assertions.assertEquals(item.getRequestId(), itemByText.getRequestId());
    }

    @Test
    void shouldFindAllByOwnerIdByPages() {
        itemRepository.save(item);

        Pageable sorted = PageRequest.of((1 / 2), 2);
        Assertions.assertEquals(1, itemRepository.findAllByOwnerIdByPages(1L, sorted).size());

        Item itemByText = itemRepository.findAllByOwnerIdByPages(1L, sorted).get(0);
        Assertions.assertEquals(1, itemByText.getId());
        Assertions.assertEquals(item.getName(), itemByText.getName());
        Assertions.assertEquals(item.getDescription(), itemByText.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemByText.getAvailable());
        Assertions.assertEquals(item.getOwnerId(), itemByText.getOwnerId());
        Assertions.assertEquals(item.getRequestId(), itemByText.getRequestId());
    }

    @Test
    void shouldSearchByTextByPages() {
        itemRepository.save(item);

        Assertions.assertEquals(1, itemRepository.findAll().size());

        Pageable sorted = PageRequest.of((1 / 2), 2);
        Item itemByText = itemRepository.searchByTextByPages("Дрель", sorted).get(0);
        Assertions.assertEquals(1, itemByText.getId());
        Assertions.assertEquals(item.getName(), itemByText.getName());
        Assertions.assertEquals(item.getDescription(), itemByText.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemByText.getAvailable());
        Assertions.assertEquals(item.getOwnerId(), itemByText.getOwnerId());
        Assertions.assertEquals(item.getRequestId(), itemByText.getRequestId());
    }
}
