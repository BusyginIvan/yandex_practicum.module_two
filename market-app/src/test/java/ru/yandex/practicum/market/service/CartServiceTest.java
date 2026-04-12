package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.CartModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class CartServiceTest extends AbstractServiceTest {
    @Autowired
    private CartService cartService;

    @Test
    void getCart_ShouldReturnItemsAndTotal() {
        when(cartItemCountR2dbcRepository.findAllByUserId(1L)).thenReturn(Flux.just(
            cartItem(1L, 2),
            cartItem(2L, 1)
        ));
        when(itemCacheService.getByIds(List.of(1L, 2L))).thenReturn(Mono.just(List.of(
            item(1L, "Apple", 100, 10L),
            item(2L, "Banana", 50, 20L)
        )));

        CartModel actual = cartService.getCart().block();

        assertNotNull(actual);
        assertEquals(2, actual.items().size());
        assertEquals(250, actual.total());
    }

    @Test
    void changeItemCount_ShouldUpdateCounterAndReturnUpdatedCart() {
        when(itemCacheService.getById(1L)).thenReturn(Mono.just(item(1L, "Apple", 100, 10L)));
        when(cartItemCountR2dbcRepository.findByUserIdAndItemId(1L, 1L)).thenReturn(Mono.empty());
        when(cartItemCountR2dbcRepository.create(org.mockito.ArgumentMatchers.any(CartItemCountR2dbcEntity.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(cartItemCountR2dbcRepository.findAllByUserId(1L)).thenReturn(Flux.just(
            cartItem(1L, 1)
        ));
        when(itemCacheService.getByIds(List.of(1L))).thenReturn(Mono.just(List.of(
            item(1L, "Apple", 100, 10L)
        )));

        CartModel actual = cartService.changeItemCount(1L, CartItemCountAction.PLUS).block();

        assertNotNull(actual);
        assertEquals(1, actual.items().size());
        assertEquals(100, actual.total());
    }

    private static ItemR2dbcEntity item(long id, String title, long price, long imageId) {
        ItemR2dbcEntity item = new ItemR2dbcEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("desc");
        item.setPrice(price);
        item.setImageId(imageId);
        return item;
    }

    private static CartItemCountR2dbcEntity cartItem(long itemId, int count) {
        CartItemCountR2dbcEntity cartItem = new CartItemCountR2dbcEntity();
        cartItem.setUserId(1L);
        cartItem.setItemId(itemId);
        cartItem.setCount(count);
        return cartItem;
    }
}
