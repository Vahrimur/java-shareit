package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectItemException;
import ru.practicum.shareit.exception.IncorrectUserException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) throws IncorrectUserException, ValidationException {
        Item item = ItemMapper.mapToItemEntity(itemDto, userId);
        userService.checkUserExist(userId);
        checkCorrectItem(item);
        item.setOwnerId(userId);
        return ItemMapper.mapToItemDto(itemRepository.createItem(item));
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) throws IncorrectUserException, IncorrectItemException {
        Item item = ItemMapper.mapToItemEntity(itemDto, userId);
        item.setId(itemId);
        userService.checkUserExist(userId);
        checkItemExist(item.getId());
        checkCorrectItemOwner(item, userId);
        item.setOwnerId(userId);
        if (item.getName() == null) {
            item.setName(itemRepository.getItemById(item.getId()).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(itemRepository.getItemById(item.getId()).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(itemRepository.getItemById(item.getId()).getAvailable());
        }
        return ItemMapper.mapToItemDto(itemRepository.updateItem(item));
    }

    @Override
    public ItemDto getItemById(Long itemId) throws IncorrectItemException {
        checkItemExist(itemId);
        return ItemMapper.mapToItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        List<Item> items =  itemRepository.getAllItems()
                .stream()
                .filter(item -> item.getOwnerId() == userId)
                .collect(Collectors.toList());
        return items.stream().map(ItemMapper::mapToItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemsByText(String text) {
        List<Item> items = new ArrayList<>();
        if (!text.equals("")) {
            items =  itemRepository.getAllItems()
                    .stream()
                    .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                            || item.getDescription().toLowerCase().contains(text.toLowerCase())
                            && item.getAvailable())
                    .collect(Collectors.toList());
        }
        return items.stream().map(ItemMapper::mapToItemDto).collect(Collectors.toList());
    }

    private void checkCorrectItem(Item item) throws ValidationException {
        if (item.getAvailable() == null || !item.getAvailable()) {
            throw new ValidationException("Вещь должна быть доступна для аренды.");
        }
        if (item.getName() == null || item.getName().equals("")) {
            throw new ValidationException("Название не может быть пустым.");
        }
        if (item.getDescription() == null) {
            throw new ValidationException("Описание не может быть пустым.");
        }
    }

    private void checkItemExist(Long id) throws IncorrectItemException {
        if (!itemRepository.getAllItems().isEmpty()) {
            List<Long> ids = itemRepository.getAllItems().stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(id)) {
                throw new IncorrectItemException("Не существует вещи с таким id.");
            }
        }  else {
            throw new IncorrectItemException("Не существует вещи с таким id.");
        }
    }

    private void checkCorrectItemOwner(Item item, Long userId) throws IncorrectUserException {
        if (!Objects.equals(itemRepository.getItemById(item.getId()).getOwnerId(), userId)) {
            throw new IncorrectUserException("Указан некорректный id владельца вещи.");
        }
    }

}
