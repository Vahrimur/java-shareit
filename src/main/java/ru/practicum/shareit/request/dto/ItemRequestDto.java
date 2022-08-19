package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private User requester;
    private LocalDateTime created;

    public ItemRequestDto(String description, User requester, LocalDateTime created) {
        this.description = description;
        this.requester = requester;
        this.created = created;
    }
}
