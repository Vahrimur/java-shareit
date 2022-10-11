package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final String error; // название ошибки
    private final String description; // подробное описание

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }
}
