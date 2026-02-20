package ru.yandex.practicum.market.exception.not_found;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(long id) {
        super("Item not found: " + id);
    }
}