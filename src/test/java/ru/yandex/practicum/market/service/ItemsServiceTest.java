package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ItemsServiceTest extends AbstractServiceTest {
    @Autowired
    private ItemsService itemsService;

    @Test
    void getItems_ShouldReturnRowsWithPaddingAndPaging() {
        ItemEntity item1 = item(1L, "Apple", "desc1", 100, 10L);
        ItemEntity item2 = item(2L, "Banana", "desc2", 200, 20L);
        PageRequest pageable = PageRequest.of(0, 5);
        when(itemRepository.findAll(ArgumentMatchers.<Specification<ItemEntity>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(item1, item2), pageable, 2));

        CartItemCountEntity counter = new CartItemCountEntity();
        counter.setItemId(1L);
        counter.setCount(3);
        when(cartItemCountRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(counter));

        ItemsPageModel actual = itemsService.getItems("", ItemSort.NO, 1, 5);

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
        assertFalse(actual.paging().hasPrevious());
        assertFalse(actual.paging().hasNext());
    }

    @Test
    void getItems_ShouldUsePriceSort() {
        PageRequest pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "price"));
        when(itemRepository.findAll(ArgumentMatchers.<Specification<ItemEntity>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(cartItemCountRepository.findAllById(List.of())).thenReturn(List.of());

        ItemsPageModel actual = itemsService.getItems("abc", ItemSort.PRICE, 2, 10);

        assertEquals(List.of(), actual.items());
        assertEquals(10, actual.paging().pageSize());
        assertEquals(2, actual.paging().pageNumber());
    }

    @Test
    void getItems_ShouldUseTitleSort() {
        PageRequest pageable = PageRequest.of(2, 5, Sort.by(Sort.Direction.ASC, "title"));
        when(itemRepository.findAll(ArgumentMatchers.<Specification<ItemEntity>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(cartItemCountRepository.findAllById(List.of())).thenReturn(List.of());

        ItemsPageModel actual = itemsService.getItems("", ItemSort.ALPHA, 3, 5);

        assertEquals(List.of(), actual.items());
        assertEquals(5, actual.paging().pageSize());
        assertEquals(3, actual.paging().pageNumber());
    }

    private static ItemEntity item(long id, String title, String description, long price, long imageId) {
        ItemEntity item = new ItemEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setImageId(imageId);
        return item;
    }
}
