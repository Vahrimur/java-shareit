package ru.practicum.shareit.exception;

public class SameEmailException extends Exception {
    public SameEmailException(String message) {
        super(message);
    }
}
