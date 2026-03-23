package ru.yandex.practicum.market.exception.validation;

import org.springframework.http.MediaType;

public class InvalidImageContentTypeException extends ValidationException {
    public InvalidImageContentTypeException(MediaType contentType) {
        super("Only image/* content types are allowed" + (contentType == null ? "" : (", got: " + contentType)));
    }
}
