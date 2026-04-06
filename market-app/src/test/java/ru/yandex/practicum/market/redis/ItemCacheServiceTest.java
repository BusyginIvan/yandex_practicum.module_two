package ru.yandex.practicum.market.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemCacheServiceTest extends AbstractCacheServiceTest {
    @Autowired
    private ItemCacheService itemCacheService;

    @Test
    void getById_WhenCached_ShouldReturnFromCache() {
        ItemR2dbcEntity item = item(1L, "Apple", 100);
        ReactiveValueOperations<String, ItemR2dbcEntity> valueOps = mock(ReactiveValueOperations.class);
        when(itemRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("item:1")).thenReturn(Mono.just(item));

        ItemR2dbcEntity actual = itemCacheService.getById(1L).block();

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        verify(itemR2dbcRepository, never()).findById(1L);
    }

    @Test
    void getById_WhenMissing_ShouldLoadFromDbAndCache() {
        ItemR2dbcEntity item = item(1L, "Apple", 100);
        ReactiveValueOperations<String, ItemR2dbcEntity> valueOps = mock(ReactiveValueOperations.class);
        when(itemRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("item:1")).thenReturn(Mono.empty());
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.just(item));
        when(valueOps.set(eq("item:1"), eq(item), any())).thenReturn(Mono.just(true));

        ItemR2dbcEntity actual = itemCacheService.getById(1L).block();

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        verify(itemR2dbcRepository).findById(1L);
        verify(valueOps).set(eq("item:1"), eq(item), any());
    }

    @Test
    void getByIds_WhenPartialCache_ShouldLoadMissingAndCache() {
        ItemR2dbcEntity item1 = item(1L, "Apple", 100);
        ItemR2dbcEntity item2 = item(2L, "Banana", 200);
        ReactiveValueOperations<String, ItemR2dbcEntity> valueOps = mock(ReactiveValueOperations.class);
        when(itemRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(List.of("item:1", "item:2")))
            .thenReturn(Mono.just(Arrays.asList(item1, null)));
        when(itemR2dbcRepository.findAllById(List.of(2L))).thenReturn(Flux.just(item2));
        when(valueOps.set(eq("item:2"), eq(item2), any())).thenReturn(Mono.just(true));

        List<ItemR2dbcEntity> actual = itemCacheService.getByIds(List.of(1L, 2L)).block();

        assertNotNull(actual);
        assertEquals(2, actual.size());
        verify(itemR2dbcRepository).findAllById(List.of(2L));
        verify(valueOps).set(eq("item:2"), eq(item2), any());
    }

    private static ItemR2dbcEntity item(long id, String title, long price) {
        ItemR2dbcEntity item = new ItemR2dbcEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("desc");
        item.setPrice(price);
        item.setImageId(10L);
        return item;
    }
}
