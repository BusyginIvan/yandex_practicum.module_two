package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.exception.not_found.ItemNotFoundException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceTest extends AbstractServiceTest {
    private static final ItemEntity ITEM = new ItemEntity();

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
        CartItemCountEntity cartItem = cartItem(3);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(ITEM));
        when(cartItemCountRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        ItemModel actual = itemService.getItem(1L);

        assertEquals(1L, actual.id());
        assertEquals("title", actual.title());
        assertEquals(3, actual.count());
    }

    @Test
    void getItem_WhenItemMissing_ShouldThrowItemNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(1L));
    }

    @Test
    void updateCartItemCount_Plus_WhenNoCartItem_ShouldCreateNewCounter() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(ITEM));
        when(cartItemCountRepository.findById(1L)).thenReturn(Optional.empty());

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.PLUS);

        assertEquals(1, actual.count());
        verify(cartItemCountRepository).save(any(CartItemCountEntity.class));
    }

    @Test
    void updateCartItemCount_Minus_WhenCountBecomesZero_ShouldDeleteCounter() {
        CartItemCountEntity cartItem = cartItem(1);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(ITEM));
        when(cartItemCountRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.MINUS);

        assertEquals(0, actual.count());
        verify(cartItemCountRepository).delete(cartItem);
    }

    @Test
    void updateCartItemCount_Delete_ShouldDeleteCounter() {
        CartItemCountEntity cartItem = cartItem(5);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(ITEM));
        when(cartItemCountRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        ItemModel actual = itemService.updateCartItemCount(1L, CartItemCountAction.DELETE);

        assertEquals(0, actual.count());
        verify(cartItemCountRepository).delete(cartItem);
    }

    private static CartItemCountEntity cartItem(int count) {
        CartItemCountEntity cartItem = new CartItemCountEntity();
        cartItem.setItem(ITEM);
        cartItem.setCount(count);
        return cartItem;
    }
}
