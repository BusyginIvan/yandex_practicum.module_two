package ru.yandex.practicum.market.api.model;

import java.util.List;

public record ItemsPageModel(
    List<List<ItemModel>> items,
    PagingModel paging
) { }
