package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.market.api.model.CartModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CartServiceTest extends AbstractServiceTest {
    @Autowired
    private CartService cartService;

    @Test
    void getCart_ShouldReturnItemsAndTotal() {
        when(cartItemCountRepository.findAllWithItems()).thenReturn(List.of(
            cartItem(item(1L, "Apple", 100, 10L), 2),
            cartItem(item(2L, "Banana", 50, 20L), 1)
        ));

        CartModel actual = cartService.getCart();

        assertEquals(2, actual.items().size());
        assertEquals(250, actual.total());
    }

    @Test
    void changeItemCount_ShouldUpdateCounterAndReturnUpdatedCart() {
        ItemEntity item = item(1L, "Apple", 100, 10L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemCountRepository.findById(1L)).thenReturn(Optional.empty());
        when(cartItemCountRepository.findAllWithItems())
            .thenReturn(List.of(cartItem(item, 1)));

        CartModel actual = cartService.changeItemCount(1L, CartItemCountAction.PLUS);

        assertEquals(1, actual.items().size());
        assertEquals(100, actual.total());
    }

    private static ItemEntity item(long id, String title, long price, long imageId) {
        ItemEntity item = new ItemEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("desc");
        item.setPrice(price);
        item.setImageId(imageId);
        return item;
    }

    private static CartItemCountEntity cartItem(ItemEntity item, int count) {
        CartItemCountEntity cartItem = new CartItemCountEntity();
        cartItem.setItem(item);
        cartItem.setItemId(item.getId());
        cartItem.setCount(count);
        return cartItem;
    }
}
