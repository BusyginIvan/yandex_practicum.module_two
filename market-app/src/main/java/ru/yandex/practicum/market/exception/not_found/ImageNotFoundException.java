package ru.yandex.practicum.market.exception.not_found;

public class ImageNotFoundException extends NotFoundException {
    public ImageNotFoundException(long id) {
        super("Image not found: " + id);
    }
}
