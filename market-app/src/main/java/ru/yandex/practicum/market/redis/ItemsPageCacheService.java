package ru.yandex.practicum.market.redis;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.time.Duration;
import java.util.List;

@Service
public class ItemsPageCacheService {
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ReactiveRedisTemplate<String, ItemsPageCache> itemsPageRedisTemplate;
    private final ItemCacheService itemCacheService;
    private final R2dbcEntityTemplate entityTemplate;

    public ItemsPageCacheService(
        ReactiveRedisTemplate<String, ItemsPageCache> itemsPageRedisTemplate,
        ItemCacheService itemCacheService,
        R2dbcEntityTemplate entityTemplate
    ) {
        this.itemsPageRedisTemplate = itemsPageRedisTemplate;
        this.itemCacheService = itemCacheService;
        this.entityTemplate = entityTemplate;
    }

    public Mono<ItemsPage> getPage(
        String search,
        ItemSort sort,
        int pageNumber,
        int pageSize
    ) {
        String key = key(search, sort, pageNumber, pageSize);

        return itemsPageRedisTemplate.opsForValue().get(key)
            .flatMap(cached -> {
                List<Long> itemIds = cached.itemIds();
                if (itemIds == null || itemIds.isEmpty()) {
                    return Mono.just(new ItemsPage(List.of(), cached.totalCount()));
                }
                return itemCacheService.getByIds(itemIds)
                    .map(items -> new ItemsPage(items, cached.totalCount()));
            })
            .switchIfEmpty(Mono.defer(() ->
                loadFromDb(search, sort, pageNumber, pageSize).flatMap(page -> {
                    List<Long> itemIds = page.items().stream()
                        .map(ItemR2dbcEntity::getId)
                        .toList();
                    ItemsPageCache cache = new ItemsPageCache(itemIds, page.totalCount());
                    return itemsPageRedisTemplate.opsForValue().set(key, cache, TTL)
                        .thenReturn(page);
                })
            ));
    }

    private Mono<ItemsPage> loadFromDb(
        String search,
        ItemSort sort,
        int pageNumber,
        int pageSize
    ) {
        Criteria criteria = toSearchCriteria(search);
        int offset = (pageNumber - 1) * pageSize;

        Query countQuery = Query.query(criteria);
        Mono<Long> totalCountMono = entityTemplate.count(countQuery, ItemR2dbcEntity.class);

        Sort sortSpec = toSpringSort(sort);
        Query selectQuery = Query.query(criteria).sort(sortSpec).limit(pageSize).offset(offset);
        Mono<List<ItemR2dbcEntity>> itemsMono =
            entityTemplate.select(selectQuery, ItemR2dbcEntity.class).collectList();

        return Mono.zip(itemsMono, totalCountMono)
            .map(tuple -> new ItemsPage(tuple.getT1(), tuple.getT2()));
    }

    private static Criteria toSearchCriteria(String search) {
        String trimmedSearch = search == null ? "" : search.trim();
        if (trimmedSearch.isEmpty()) return Criteria.empty();
        String pattern = "%" + trimmedSearch + "%";
        return Criteria.where("title").like(pattern).ignoreCase(true)
            .or(Criteria.where("description").like(pattern).ignoreCase(true));
    }

    private static Sort toSpringSort(ItemSort sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };
    }

    private static String key(String search, ItemSort sort, int pageNumber, int pageSize) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
        StringBuilder builder = new StringBuilder("items");
        builder.append(":page=").append(pageNumber);
        builder.append(":size=").append(pageSize);

        if (!normalizedSearch.isEmpty()) {
            builder.append(":search=").append(normalizedSearch);
        }

        if (sort != null && sort != ItemSort.NO) {
            builder.append(":sort=").append(sort.name());
        }

        return builder.toString();
    }
}
