package ru.yandex.practicum.market.redis;

import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.util.List;

public record ItemsPage(
    List<ItemR2dbcEntity> items,
    long totalCount
) { }
