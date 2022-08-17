package ru.practicum.shareit.item;

import java.util.List;

public interface ItemRepository {
    Item createItem(Item item);
    Item updateItem(Item item);
    Item getItemById(long id);
    List<Item> getAllItems();
}
