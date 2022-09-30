package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
                "Incorrect field error", e.getMessage()
        );
    }

    @ExceptionHandler(IncorrectObjectException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncorrectObjectException(final IncorrectObjectException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Incorrect object error", e.getMessage()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Missing request header error", e.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Missing request parameter error", e.getMessage()
        );
    }

    @ExceptionHandler(IncorrectEnumException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseShort handleIncorrectEnumException(final IncorrectEnumException e) {
        log.warn(e.getMessage());
        return new ErrorResponseShort(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn(e.getMessage());
        return new ErrorResponse(
                "Incorrect parameter error", e.getMessage()
        );
    }
}
