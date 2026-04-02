package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.redis.ItemsPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ItemsServiceTest extends AbstractServiceTest {
    @Autowired
    private ItemsService itemsService;

    @Test
    void getItems_ShouldReturnRowsWithPaddingAndPaging() {
        ItemR2dbcEntity item1 = item(1L, "Apple", "desc1", 100, 10L);
        ItemR2dbcEntity item2 = item(2L, "Banana", "desc2", 200, 20L);
        when(itemsPageCacheService.getPage("", ItemSort.NO, 1, 5))
            .thenReturn(Mono.just(new ItemsPage(List.of(item1, item2), 2L)));

        CartItemCountR2dbcEntity counter = new CartItemCountR2dbcEntity();
        counter.setItemId(1L);
        counter.setCount(3);
        when(cartItemCountR2dbcRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(Flux.just(counter));

        ItemsPageModel actual = itemsService.getItems("", ItemSort.NO, 1, 5).block();

        assertNotNull(actual);
        assertEquals(1, actual.items().size());

        List<ItemModel> row = actual.items().getFirst();
        assertEquals(3, row.size());
        ItemModel first = row.get(0);
        ItemModel second = row.get(1);
        ItemModel padding = row.get(2);
        assertEquals("Apple", first.title());
        assertEquals(3, first.count());
        assertEquals("Banana", second.title());
        assertEquals(0, second.count());
        assertEquals(-1L, padding.id());

        PagingModel paging = actual.paging();
        assertEquals(5, paging.pageSize());
        assertEquals(1, paging.pageNumber());
        assertFalse(paging.hasPrevious());
        assertFalse(paging.hasNext());
    }

    @Test
    void getItems_ThereArePreviousAndNextPages() {
        ItemR2dbcEntity item = item(1L, "Apple", "desc", 100, 10L);
        when(itemsPageCacheService.getPage("", ItemSort.NO, 2, 1))
            .thenReturn(Mono.just(new ItemsPage(List.of(item), 3L)));
        when(cartItemCountR2dbcRepository.findAllById(List.of(1L)))
            .thenReturn(Flux.empty());

        ItemsPageModel actual = itemsService.getItems("", ItemSort.NO, 2, 1).block();

        assertNotNull(actual);
        assertEquals(1, actual.items().size());
        List<ItemModel> row = actual.items().getFirst();
        assertEquals(3, row.size());
        assertEquals("Apple", row.get(0).title());
        assertEquals(-1L, row.get(1).id());
        assertEquals(-1L, row.get(2).id());

        PagingModel paging = actual.paging();
        assertEquals(1, paging.pageSize());
        assertEquals(2, paging.pageNumber());
        assertTrue(paging.hasPrevious());
        assertTrue(paging.hasNext());
    }

    private static ItemR2dbcEntity item(long id, String title, String description, long price, long imageId) {
        ItemR2dbcEntity item = new ItemR2dbcEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setImageId(imageId);
        return item;
    }
}
