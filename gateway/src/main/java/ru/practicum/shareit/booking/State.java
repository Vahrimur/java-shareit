package ru.practicum.shareit.booking;

public enum State {
    ALL, //значение по умолчанию, все бронирования
    CURRENT, //текущие бронирования
    PAST, // завершенные бронирования
    FUTURE, //будущие бронирования
    WAITING, //бронирования, ожидающие подтверждения
    REJECTED //отклоненные бронирования
}
