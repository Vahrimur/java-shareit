package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(IncorrectFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectFieldException(final IncorrectFieldException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Ошибка поля объекта", e.getMessage()
        );
    }

    @ExceptionHandler(SameEmailException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleSameEmailException(final SameEmailException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Ошибка повтора имейла", e.getMessage()
        );
    }

    @ExceptionHandler(IncorrectObjectException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncorrectObjectException(final IncorrectObjectException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Ошибка объекта", e.getMessage()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Ошибка заголовка", e.getMessage()
        );
    }
}
