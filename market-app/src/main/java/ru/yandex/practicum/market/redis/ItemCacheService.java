package ru.yandex.practicum.market.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;

import java.time.Duration;

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

    private String key(long id) {
        return "item:" + id;
    }
}
