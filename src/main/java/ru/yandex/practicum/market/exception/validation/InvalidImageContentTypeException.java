package ru.yandex.practicum.market.exception.validation;

public class InvalidImageContentTypeException extends ValidationException {
    public InvalidImageContentTypeException(String contentType) {
        super("Only image/* content types are allowed" +
            (contentType == null || contentType.isBlank() ? "" : (", got: " + contentType)));
    }
}
