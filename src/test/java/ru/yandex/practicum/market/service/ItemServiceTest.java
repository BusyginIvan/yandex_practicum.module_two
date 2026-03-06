package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.exception.not_found.ItemNotFoundException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceTest extends AbstractServiceTest {
    private static final ItemR2dbcEntity ITEM = new ItemR2dbcEntity();

    static {
        ITEM.setId(1L);
        ITEM.setTitle("title");
        ITEM.setDescription("description");
        ITEM.setPrice(100);
        ITEM.setImageId(10L);
    }

    @Autowired
    private ItemService itemService;

    @Test
    void getItem_ShouldReturnItemWithCount() {
        CartItemCountR2dbcEntity cartItem = cartItem(3);
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.just(ITEM));
        when(cartItemCountR2dbcRepository.findById(1L)).thenReturn(Mono.just(cartItem));

        ItemModel actual = itemService.getItem(1L).block();

        assertNotNull(actual);
        assertEquals(1L, actual.id());
        assertEquals("title", actual.title());
        assertEquals(3, actual.count());
    }

    @Test
    void getItem_WhenItemMissing_ShouldThrowItemNotFoundException() {
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.empty());
        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(1L).block());
    }

    @Test
    void updateCartItemCount_Plus_WhenNoCartItem_ShouldCreateNewCounter() {
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.just(ITEM));
        when(cartItemCountR2dbcRepository.findById(1L)).thenReturn(Mono.empty());
        when(cartItemCountR2dbcRepository.create(any(CartItemCountR2dbcEntity.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.PLUS).block();

        assertNotNull(actual);
        assertEquals(1, actual.count());
        verify(cartItemCountR2dbcRepository).create(any(CartItemCountR2dbcEntity.class));
    }

    @Test
    void updateCartItemCount_Minus_WhenCountBecomesZero_ShouldDeleteCounter() {
        CartItemCountR2dbcEntity cartItem = cartItem(1);
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.just(ITEM));
        when(cartItemCountR2dbcRepository.findById(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemCountR2dbcRepository.deleteById(1L)).thenReturn(Mono.empty());

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.MINUS).block();

        assertNotNull(actual);
        assertEquals(0, actual.count());
        verify(cartItemCountR2dbcRepository).deleteById(1L);
    }

    @Test
    void updateCartItemCount_Delete_ShouldDeleteCounter() {
        CartItemCountR2dbcEntity cartItem = cartItem(5);
        when(itemR2dbcRepository.findById(1L)).thenReturn(Mono.just(ITEM));
        when(cartItemCountR2dbcRepository.findById(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemCountR2dbcRepository.deleteById(1L)).thenReturn(Mono.empty());

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.DELETE).block();

        assertNotNull(actual);
        assertEquals(0, actual.count());
        verify(cartItemCountR2dbcRepository).deleteById(1L);
    }

    private static CartItemCountR2dbcEntity cartItem(int count) {
        CartItemCountR2dbcEntity cartItem = new CartItemCountR2dbcEntity();
        cartItem.setItemId(1L);
        cartItem.setCount(count);
        return cartItem;
    }
}
