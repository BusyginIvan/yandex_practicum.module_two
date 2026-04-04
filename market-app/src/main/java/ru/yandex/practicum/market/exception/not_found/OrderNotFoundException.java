package ru.yandex.practicum.market.exception.not_found;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(long id) {
        super("Order not found: " + id);
    }
}
