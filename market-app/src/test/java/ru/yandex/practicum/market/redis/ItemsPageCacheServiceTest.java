package ru.yandex.practicum.market.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemsPageCacheServiceTest extends AbstractCacheServiceTest {
    @Autowired
    private ItemsPageCacheService itemsPageCacheService;

    @Test
    void getPage_WhenCached_ShouldReturnFromCache() {
        ItemR2dbcEntity item = item(1L, "Apple", 100);
        ReactiveValueOperations<String, ItemsPageCache> valueOps = mock(ReactiveValueOperations.class);
        when(itemsPageRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString()))
            .thenReturn(Mono.just(new ItemsPageCache(List.of(1L), 10L)));
        ReactiveValueOperations<String, ItemR2dbcEntity> itemValueOps = mock(ReactiveValueOperations.class);
        when(itemRedisTemplate.opsForValue()).thenReturn(itemValueOps);
        when(itemValueOps.multiGet(List.of("item:1"))).thenReturn(Mono.just(List.of(item)));

        ItemsPage page = itemsPageCacheService.getPage("", ItemSort.NO, 1, 5).block();

        assertNotNull(page);
        assertEquals(1, page.items().size());
        assertEquals(10L, page.totalCount());
        verify(r2dbcEntityTemplate, never()).select(any(Query.class), eq(ItemR2dbcEntity.class));
    }

    @Test
    void getPage_WhenCachedEmpty_ShouldNotCallItemCache() {
        ReactiveValueOperations<String, ItemsPageCache> valueOps = mock(ReactiveValueOperations.class);
        when(itemsPageRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString()))
            .thenReturn(Mono.just(new ItemsPageCache(List.of(), 0L)));

        ItemsPage page = itemsPageCacheService.getPage("", ItemSort.NO, 1, 5).block();

        assertNotNull(page);
        assertEquals(0, page.items().size());
        verify(itemRedisTemplate, never()).opsForValue();
    }

    @Test
    void getPage_WhenMissing_ShouldLoadFromDbAndCache() {
        ItemR2dbcEntity item1 = item(1L, "Apple", 100);
        ItemR2dbcEntity item2 = item(2L, "Banana", 200);
        ReactiveValueOperations<String, ItemsPageCache> valueOps = mock(ReactiveValueOperations.class);
        when(itemsPageRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(r2dbcEntityTemplate.count(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Mono.just(2L));
        when(r2dbcEntityTemplate.select(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Flux.just(item1, item2));
        when(valueOps.set(anyString(), any(ItemsPageCache.class), any()))
            .thenReturn(Mono.just(true));

        ItemsPage page = itemsPageCacheService.getPage("", ItemSort.NO, 1, 5).block();

        assertNotNull(page);
        assertEquals(2, page.items().size());
        assertEquals(2L, page.totalCount());
        verify(valueOps).set(anyString(), any(ItemsPageCache.class), any());
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
