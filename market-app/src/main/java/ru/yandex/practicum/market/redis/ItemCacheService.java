package ru.yandex.practicum.market.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ItemCacheService {
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ReactiveRedisTemplate<String, ItemR2dbcEntity> itemRedisTemplate;
    private final ItemR2dbcRepository itemRepository;

    public ItemCacheService(
        ReactiveRedisTemplate<String, ItemR2dbcEntity> itemRedisTemplate,
        ItemR2dbcRepository itemRepository
    ) {
        this.itemRedisTemplate = itemRedisTemplate;
        this.itemRepository = itemRepository;
    }

    public Mono<ItemR2dbcEntity> getById(long id) {
        String key = key(id);
        return itemRedisTemplate.opsForValue().get(key).switchIfEmpty(Mono.defer(() ->
            itemRepository.findById(id).flatMap(item ->
                itemRedisTemplate.opsForValue().set(key, item, TTL).thenReturn(item)
            )
        ));
    }

    public Mono<List<ItemR2dbcEntity>> getByIds(List<Long> ids) {
        if (ids.isEmpty()) return Mono.just(List.of());

        List<String> keys = ids.stream().map(this::key).toList();
        return itemRedisTemplate.opsForValue().multiGet(keys).flatMap(cachedItems -> {
            List<Long> missingIds = new ArrayList<>();
            List<ItemR2dbcEntity> items = new ArrayList<>();

            for (int i = 0; i < ids.size(); i++) {
                ItemR2dbcEntity cachedItem = cachedItems.get(i);
                if (cachedItem == null) {
                    missingIds.add(ids.get(i));
                } else {
                    items.add(cachedItem);
                }
            }

            if (missingIds.isEmpty()) return Mono.just(items);

            return itemRepository.findAllById(missingIds).collectList().flatMap(dbItems -> {
                items.addAll(dbItems);

                Mono<Void> cacheItems = Flux.fromIterable(dbItems)
                    .filter(item -> Objects.nonNull(item.getId()))
                    .flatMap(item -> itemRedisTemplate.opsForValue().set(key(item.getId()), item, TTL))
                    .then();

                return cacheItems.thenReturn(items);
            });
        });
    }

    private String key(long id) {
        return "item:" + id;
    }
}
