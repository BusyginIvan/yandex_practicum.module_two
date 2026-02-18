package ru.yandex.practicum.market.api.model;

public record OrderItemModel(
    long id,
    String title,
    long price,
    int count
) { }
