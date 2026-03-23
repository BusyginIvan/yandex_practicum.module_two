package ru.yandex.practicum.market.exception.validation;

public class ImageRequiredException extends ValidationException {
    public ImageRequiredException() {
        super("image is required");
    }
}
