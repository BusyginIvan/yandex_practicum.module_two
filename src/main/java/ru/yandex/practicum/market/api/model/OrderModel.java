package ru.yandex.practicum.market.api.model;

import java.util.List;

public record OrderModel(
    long id,
    List<OrderItemModel> items,
    long totalSum
) { }
