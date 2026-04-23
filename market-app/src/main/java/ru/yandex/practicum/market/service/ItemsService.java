package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.redis.ItemsPageCacheService;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemsService {
    private static final int ITEMS_PER_ROW = 3;
    private static final ItemModel EMPTY_ITEM = new ItemModel(
        -1,
        "",
        "",
        0,
        1,
        0
    );

    private final ItemsPageCacheService itemsPageCacheService;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;
    private final CurrentUserService currentUserService;

    public ItemsService(
        ItemsPageCacheService itemsPageCacheService,
        CartItemCountR2dbcRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper,
        CurrentUserService currentUserService
    ) {
        this.itemsPageCacheService = itemsPageCacheService;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
        this.currentUserService = currentUserService;
    }

    public Mono<ItemsPageModel> getItems(
        String search,
        ItemSort sort,
        int pageNumber,
        int pageSize
    ) {
        return itemsPageCacheService.getPage(search, sort, pageNumber, pageSize).flatMap(page -> {
            List<ItemR2dbcEntity> items = page.items();
            long totalCount = page.totalCount();
            boolean hasPrevious = pageNumber > 1;
            long totalPages = totalCount == 0 ? 0 : (totalCount - 1) / pageSize + 1;
            boolean hasNext = pageNumber < totalPages;

            if (items.isEmpty()) {
                return Mono.just(new ItemsPageModel(
                    List.of(),
                    new PagingModel(pageSize, pageNumber, hasPrevious, hasNext)
                ));
            }

            List<Long> itemIds = items.stream().map(ItemR2dbcEntity::getId).toList();
            return loadCartCounts(itemIds).map(counts -> {
                List<ItemModel> itemModels = items.stream().map(item ->
                    itemModelMapper.toItemModel(item, counts.getOrDefault(item.getId(), 0))
                ).toList();
                return new ItemsPageModel(
                    toRows(itemModels),
                    new PagingModel(pageSize, pageNumber, hasPrevious, hasNext)
                );
            });
        });
    }

    private Mono<Map<Long, Integer>> loadCartCounts(List<Long> itemIds) {
        return currentUserService.getCurrentUserIdOrEmpty()
            .flatMap(userId -> cartItemCountRepository.findAllByUserIdAndItemIdIn(userId, itemIds)
                .collectMap(CartItemCountR2dbcEntity::getItemId, CartItemCountR2dbcEntity::getCount))
            .defaultIfEmpty(Map.of());
    }

    private static List<List<ItemModel>> toRows(List<ItemModel> items) {
        List<List<ItemModel>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            int end = Math.min(i + ITEMS_PER_ROW, items.size());
            List<ItemModel> row = new ArrayList<>(items.subList(i, end));
            while (row.size() < ITEMS_PER_ROW) {
                row.add(EMPTY_ITEM);
            }
            rows.add(row);
        }
        return rows;
    }
}
