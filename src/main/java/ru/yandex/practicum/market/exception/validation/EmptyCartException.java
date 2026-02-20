package ru.yandex.practicum.market.exception.validation;

public class EmptyCartException extends ValidationException {
    public EmptyCartException() {
        super("Cart is empty");
    }
}
