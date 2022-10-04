package ru.practicum.shareit.exception;

public class ErrorResponseShort {
    private final String error;

    public ErrorResponseShort(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
