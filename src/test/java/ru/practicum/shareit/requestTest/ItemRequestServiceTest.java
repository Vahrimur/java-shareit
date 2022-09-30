package ru.practicum.shareit.requestTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.IncorrectFieldException;
import ru.practicum.shareit.exception.IncorrectObjectException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetDto;
import ru.practicum.shareit.request.dto.ItemRequestForGetMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserService userService;
    @Mock
    ItemRepository itemRepository;
    private ItemRequestService itemRequestService;
    private MockitoSession session;

    private final User requester = new User(1L, "user", "user@user.com");
    private final UserDto requesterDto = UserMapper.mapToUserDto(requester);
    private final ItemRequest itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться дрелью",
            requester, LocalDateTime.of(2022, Month.SEPTEMBER, 8, 12, 30, 30));

    private final ItemRequestDto itemRequestDto = ItemRequestMapper.mapToItemRequestDto(itemRequest);
    private final Item item = new Item(
            1L, "Дрель", "Простая дрель", true, 2L, 1L);
    private final ItemDto itemDto = ItemMapper.mapToItemDto(item);
    private final ItemRequestForGetDto itemRequestForGetDto = ItemRequestForGetMapper.mapToItemRequestForGetDto(
            itemRequest, List.of(itemDto));

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        itemRequestService = new ItemRequestServiceImpl(
                itemRequestRepository,
                userService,
                itemRepository
        );
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    void shouldAddNewItemRequest() throws IncorrectObjectException, IncorrectFieldException {
        Mockito
                .when(userService.getUserById(1L))
                .thenReturn(requesterDto);
        Mockito
                .when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);

        ItemRequestDto newRequest = itemRequestService.addNewItemRequest(1L, itemRequestDto);

        Assertions.assertEquals(itemRequestDto.getId(), newRequest.getId());
        Assertions.assertEquals(itemRequestDto.getDescription(), newRequest.getDescription());
        Assertions.assertEquals(itemRequestDto.getCreated(), newRequest.getCreated());

        Mockito
                .verify(userService, Mockito.times(1))
                .getUserById(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .save(any());
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequestsByRequesterId() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAllByRequesterId(1L))
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestId(1L))
                .thenReturn(List.of(item));

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequestsByRequesterId(1L);

        Assertions.assertEquals(itemRequestForGetDto.getId(), requests.get(0).getId());
        Assertions.assertEquals(itemRequestForGetDto.getDescription(), requests.get(0).getDescription());
        Assertions.assertEquals(itemRequestForGetDto.getCreated(), requests.get(0).getCreated());
        Assertions.assertEquals(itemRequestForGetDto.getItems().get(0), requests.get(0).getItems().get(0));

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAllByRequesterId(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByRequestId(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequestsByRequesterIdEmpty() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAllByRequesterId(1L))
                .thenReturn(new ArrayList<>());

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequestsByRequesterId(1L);

        Assertions.assertEquals(0, requests.size());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAllByRequesterId(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequests() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAll(1L))
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestId(1L))
                .thenReturn(List.of(item));

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequests(1L, null, null);

        Assertions.assertEquals(itemRequestForGetDto.getId(), requests.get(0).getId());
        Assertions.assertEquals(itemRequestForGetDto.getDescription(), requests.get(0).getDescription());
        Assertions.assertEquals(itemRequestForGetDto.getCreated(), requests.get(0).getCreated());
        Assertions.assertEquals(itemRequestForGetDto.getItems().get(0), requests.get(0).getItems().get(0));

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAll(1L);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByRequestId(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequestsEmpty() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAll(1L))
                .thenReturn(new ArrayList<>());

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequests(1L, null, null);

        Assertions.assertEquals(0, requests.size());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAll(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequestsByPages() throws IncorrectObjectException {
        Integer from = 1;
        Integer size = 1;
        Pageable sortedDesc = PageRequest.of((from / size), size, Sort.by("created").descending());
        Mockito
                .when(itemRequestRepository.findAllByPages(1L, sortedDesc))
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestId(1L))
                .thenReturn(List.of(item));

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequests(1L, from, size);

        Assertions.assertEquals(itemRequestForGetDto.getId(), requests.get(0).getId());
        Assertions.assertEquals(itemRequestForGetDto.getDescription(), requests.get(0).getDescription());
        Assertions.assertEquals(itemRequestForGetDto.getCreated(), requests.get(0).getCreated());
        Assertions.assertEquals(itemRequestForGetDto.getItems().get(0), requests.get(0).getItems().get(0));

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAllByPages(1L, sortedDesc);
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByRequestId(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetAllItemRequestsByPagesEmpty() throws IncorrectObjectException {
        Integer from = 1;
        Integer size = 1;
        Pageable sortedDesc = PageRequest.of((from / size), size, Sort.by("created").descending());
        Mockito
                .when(itemRequestRepository.findAllByPages(1L, sortedDesc))
                .thenReturn(new ArrayList<>());

        List<ItemRequestForGetDto> requests = itemRequestService.getAllItemRequests(1L, from, size);

        Assertions.assertEquals(0, requests.size());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAllByPages(1L, sortedDesc);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldCheckPageableParamsFails() {
        final IllegalArgumentException exception1 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> itemRequestService.getAllItemRequests(1L, -1, 1));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .findAllByRequestId(anyLong());
        Assertions.assertEquals("Index of start element cannot be less zero", exception1.getMessage());

        final IllegalArgumentException exception2 = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> itemRequestService.getAllItemRequests(1L, 1, 0));

        Mockito
                .verify(itemRepository, Mockito.times(0))
                .findAllByRequestId(anyLong());
        Assertions.assertEquals("Page size cannot be less or equal zero", exception2.getMessage());
    }

    @Test
    void shouldGetItemRequestById() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAll())
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestId(1L))
                .thenReturn(List.of(item));
        Mockito
                .when(itemRequestRepository.getById(1L))
                .thenReturn(itemRequest);

        ItemRequestForGetDto foundRequest = itemRequestService.getItemRequestById(1L, 1L);

        Assertions.assertEquals(itemRequestForGetDto.getId(), foundRequest.getId());
        Assertions.assertEquals(itemRequestForGetDto.getDescription(), foundRequest.getDescription());
        Assertions.assertEquals(itemRequestForGetDto.getCreated(), foundRequest.getCreated());
        Assertions.assertEquals(itemRequestForGetDto.getItems().get(0), foundRequest.getItems().get(0));

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verify(itemRepository, Mockito.times(1))
                .findAllByRequestId(1L);
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetItemRequestByIdFails1() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAll())
                .thenReturn(List.of(itemRequest));

        final IncorrectObjectException exception1 = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemRequestService.getItemRequestById(1L, 99L));
        Assertions.assertEquals("There is no item request with such ID", exception1.getMessage());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(2))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldGetItemRequestByIdFails2() throws IncorrectObjectException {
        Mockito
                .when(itemRequestRepository.findAll())
                .thenReturn(new ArrayList<>());

        final IncorrectObjectException exception1 = Assertions.assertThrows(
                IncorrectObjectException.class,
                () -> itemRequestService.getItemRequestById(1L, 1L));
        Assertions.assertEquals("There is no item request with such ID", exception1.getMessage());

        Mockito
                .verify(userService, Mockito.times(1))
                .checkUserExist(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(1))
                .findAll();
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }

    @Test
    void shouldCheckCorrectDescriptionFails() throws IncorrectObjectException {
        Mockito
                .when(userService.getUserById(1L))
                .thenReturn(requesterDto);

        itemRequestDto.setDescription("");
        final IncorrectFieldException exception = Assertions.assertThrows(
                IncorrectFieldException.class,
                () -> itemRequestService.addNewItemRequest(1L, itemRequestDto));

        Mockito
                .verify(userService, Mockito.times(1))
                .getUserById(1L);
        Mockito
                .verify(itemRequestRepository, Mockito.times(0))
                .save(any());
        Assertions.assertEquals("The description of the item request cannot be empty", exception.getMessage());
        Mockito
                .verifyNoMoreInteractions(
                        itemRequestRepository,
                        userService,
                        itemRepository
                );
    }
}
