package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ItemsServiceTest extends AbstractServiceTest {
    @Autowired
    private ItemsService itemsService;

    @Test
    void getItems_ShouldReturnRowsWithPaddingAndPaging() {
        ItemR2dbcEntity item1 = item(1L, "Apple", "desc1", 100, 10L);
        ItemR2dbcEntity item2 = item(2L, "Banana", "desc2", 200, 20L);
        when(r2dbcEntityTemplate.select(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Flux.just(item1, item2));
        when(r2dbcEntityTemplate.count(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Mono.just(2L));

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
        when(r2dbcEntityTemplate.select(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Flux.just(item));
        when(r2dbcEntityTemplate.count(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Mono.just(3L));
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

    @Test
    void getItems_WithPriceSort() {
        ArgumentCaptor<Query> selectCaptor = ArgumentCaptor.forClass(Query.class);
        when(r2dbcEntityTemplate.select(selectCaptor.capture(), eq(ItemR2dbcEntity.class)))
            .thenReturn(Flux.empty());
        when(r2dbcEntityTemplate.count(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Mono.just(0L));

        ItemsPageModel actual = itemsService.getItems("abc", ItemSort.PRICE, 2, 10).block();

        Query selectQuery = selectCaptor.getValue();
        assertEquals(Sort.by(Sort.Direction.ASC, "price"), selectQuery.getSort());
        assertEquals(10, selectQuery.getLimit());
        assertEquals(10, selectQuery.getOffset());
        assertNotNull(actual);
        assertEquals(List.of(), actual.items());
    }

    @Test
    void getItems_WithTitleSort() {
        ArgumentCaptor<Query> selectCaptor = ArgumentCaptor.forClass(Query.class);
        when(r2dbcEntityTemplate.select(selectCaptor.capture(), eq(ItemR2dbcEntity.class)))
            .thenReturn(Flux.empty());
        when(r2dbcEntityTemplate.count(any(Query.class), eq(ItemR2dbcEntity.class)))
            .thenReturn(Mono.just(0L));

        ItemsPageModel actual = itemsService.getItems("", ItemSort.ALPHA, 3, 5).block();

        Query selectQuery = selectCaptor.getValue();
        assertEquals(Sort.by(Sort.Direction.ASC, "title"), selectQuery.getSort());
        assertEquals(5, selectQuery.getLimit());
        assertEquals(10, selectQuery.getOffset());
        assertNotNull(actual);
        assertEquals(List.of(), actual.items());
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
