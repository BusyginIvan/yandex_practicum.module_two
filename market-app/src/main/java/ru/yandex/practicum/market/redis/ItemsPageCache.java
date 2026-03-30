package ru.yandex.practicum.market.redis;

import java.util.List;

public record ItemsPageCache(
    List<Long> itemIds,
    long totalCount
) { }
