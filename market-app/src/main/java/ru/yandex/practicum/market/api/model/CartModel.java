package ru.yandex.practicum.market.api.model;

import java.util.List;

public record CartModel(
    List<ItemModel> items,
    long total
) {
}
