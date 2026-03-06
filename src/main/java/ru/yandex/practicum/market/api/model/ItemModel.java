package ru.yandex.practicum.market.api.model;

public record ItemModel(
    long id,
    String title,
    String description,
    long price,
    long imageId,
    int count
) {
    public long getSubtotal() { return price * count; }
}
