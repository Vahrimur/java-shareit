package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;

    private final UserService userService;

    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserService userService, ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userService = userService;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto addNewItemRequest(Long requesterId, ItemRequestDto itemRequestDto) throws IncorrectObjectException, IncorrectFieldException {
        User requester = UserMapper.mapToUserEntity(userService.getUserById(requesterId));
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequestEntity(itemRequestDto, requester);
        checkCorrectDescription(itemRequest);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.mapToItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestForGetDto> getAllItemRequestsByRequesterId(Long requesterId) throws IncorrectObjectException {
        userService.checkUserExist(requesterId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(requesterId);
        List<ItemRequestForGetDto> requestDtos = new ArrayList<>();
        if (requests.isEmpty()) {
            return requestDtos;
        }
        for (ItemRequest itemRequest : requests) {
            Long requestId = itemRequest.getId();
            List<ItemDto> items = ItemMapper.mapToItemDto(itemRepository.findAllByRequestId(requestId));
            requestDtos.add(ItemRequestForGetMapper.mapToItemRequestForGetDto(itemRequest, items));
        }
        return requestDtos.stream()
                .sorted(Comparator.comparing(ItemRequestForGetDto::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestForGetDto> getAllItemRequests(Long requesterId) throws IncorrectObjectException {
        userService.checkUserExist(requesterId);
        List<ItemRequest> requests = itemRequestRepository.findAll(requesterId);
        List<ItemRequestForGetDto> requestDtos = new ArrayList<>();
        if (requests.isEmpty()) {
            return requestDtos;
        }
        for (ItemRequest itemRequest : requests) {
            Long requestId = itemRequest.getId();
            List<ItemDto> items = ItemMapper.mapToItemDto(itemRepository.findAllByRequestId(requestId));
            requestDtos.add(ItemRequestForGetMapper.mapToItemRequestForGetDto(itemRequest, items));
        }
        return requestDtos;
    }

    @Override
    public List<ItemRequestForGetDto> getAllItemRequestsByPages(Long requesterId, Integer from, Integer size) throws IncorrectObjectException {
        userService.checkUserExist(requesterId);
        checkPageableParams(from, size);
        Pageable sortedDesc = PageRequest.of((from / size), size, Sort.by("created").descending());
        List<ItemRequest> requests = itemRequestRepository.findAllByPages(requesterId, sortedDesc);
        List<ItemRequestForGetDto> requestDtos = new ArrayList<>();
        if (requests.isEmpty()) {
            return requestDtos;
        }
        for (ItemRequest itemRequest : requests) {
            Long requestId = itemRequest.getId();
            List<ItemDto> items = ItemMapper.mapToItemDto(itemRepository.findAllByRequestId(requestId));
            requestDtos.add(ItemRequestForGetMapper.mapToItemRequestForGetDto(itemRequest, items));
        }
        return requestDtos;
    }

    private void checkPageableParams(Integer from, Integer size) {
        if (from < 0) {
            throw new IllegalArgumentException("Index of start element cannot be less zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size cannot be less or equal zero");
        }
    }

    @Override
    public ItemRequestForGetDto getItemRequestById(Long requesterId, Long requestId) throws IncorrectObjectException {
        userService.checkUserExist(requesterId);
        checkItemRequestExists(requestId);
        List<ItemDto> items = ItemMapper.mapToItemDto(itemRepository.findAllByRequestId(requestId));
        return ItemRequestForGetMapper.mapToItemRequestForGetDto(itemRequestRepository.getById(requestId), items);
    }

    private void checkItemRequestExists(Long itemRequestId) throws IncorrectObjectException {
        if (!itemRequestRepository.findAll().isEmpty()) {
            List<Long> ids = itemRequestRepository.findAll().stream()
                    .map(ItemRequest::getId)
                    .collect(Collectors.toList());
            if (!ids.contains(itemRequestId)) {
                throw new IncorrectObjectException("There is no item request with such ID");
            }
        } else {
            throw new IncorrectObjectException("There is no item request with such ID");
        }
    }

    private void checkCorrectDescription(ItemRequest itemRequest) throws IncorrectFieldException {
        if (itemRequest.getDescription() == null || itemRequest.getDescription().equals("")) {
            throw new IncorrectFieldException("The description of the item request cannot be empty");
        }
    }
}
