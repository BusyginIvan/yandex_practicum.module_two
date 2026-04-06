package ru.yandex.practicum.market.api.model;

public record PagingModel(
    int pageSize,
    int pageNumber,
    boolean hasPrevious,
    boolean hasNext
) { }
