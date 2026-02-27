package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.market.api.model.OrderModel;
import ru.yandex.practicum.market.exception.not_found.OrderNotFoundException;
import ru.yandex.practicum.market.exception.validation.EmptyCartException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.entity.OrderEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountId;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdersServiceTest extends AbstractServiceTest {
    @Autowired
    private OrdersService ordersService;

    @Test
    void getOrders_WhenNoOrders_ShouldReturnEmptyList() {
        when(orderRepository.findAll(any(Sort.class))).thenReturn(List.of());
        List<OrderModel> actual = ordersService.getOrders();
        assertEquals(List.of(), actual);
    }

    @Test
    void getOrder_WhenNotFound_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> ordersService.getOrder(1L));
    }

    @Test
    void buy_WhenCartIsEmpty_ShouldThrowEmptyCartException() {
        when(cartItemCountRepository.findAllWithItems()).thenReturn(List.of());
        assertThrows(EmptyCartException.class, () -> ordersService.buy());
    }

    @Test
    void buy_ShouldCreateOrderItemsAndClearCart() {
        CartItemCountEntity first = cartItem(item(1L, "Apple", 100), 2);
        CartItemCountEntity second = cartItem(item(2L, "Banana", 50), 1);
        when(cartItemCountRepository.findAllWithItems()).thenReturn(List.of(first, second));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(11L);
            return order;
        });

        long orderId = ordersService.buy();

        assertEquals(11L, orderId);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderItemCountRepository).saveAll(any(List.class));
        verify(cartItemCountRepository).deleteAllInBatch(eq(List.of(first, second)));
    }

    @Test
    void getOrders_ShouldMapItemsForEachOrder() {
        OrderEntity order = new OrderEntity();
        order.setId(2L);
        order.setTotalSum(250);
        when(orderRepository.findAll(any(Sort.class))).thenReturn(List.of(order));

        OrderItemCountEntity orderItem = new OrderItemCountEntity();
        orderItem.setOrder(order);
        orderItem.setItem(item(1L, "Apple", 100));
        orderItem.setCount(2);
        orderItem.setId(new OrderItemCountId(2L, 1L));
        when(orderItemCountRepository.findAllByOrderIdsWithItems(List.of(2L))).thenReturn(List.of(orderItem));

        List<OrderModel> actual = ordersService.getOrders();

        assertEquals(1, actual.size());
        OrderModel actualOrder = actual.getFirst();
        assertEquals(2L, actualOrder.id());
        assertEquals(250, actualOrder.totalSum());
        assertEquals(1, actualOrder.items().size());
        assertEquals("Apple", actualOrder.items().getFirst().title());
    }

    private static ItemEntity item(long id, String title, long price) {
        ItemEntity item = new ItemEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("desc");
        item.setPrice(price);
        item.setImageId(1L);
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
