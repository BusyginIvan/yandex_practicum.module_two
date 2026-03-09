package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.OrderModel;
import ru.yandex.practicum.market.exception.not_found.OrderNotFoundException;
import ru.yandex.practicum.market.exception.validation.EmptyCartException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.OrderR2dbcEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdersServiceTest extends AbstractServiceTest {
    @Autowired
    private OrdersService ordersService;

    @Test
    void getOrders_WhenNoOrders_ShouldReturnEmptyList() {
        when(orderR2dbcRepository.findAllByOrderByIdDesc()).thenReturn(Flux.empty());
        when(orderItemCountR2dbcRepository.findAll()).thenReturn(Flux.empty());

        List<OrderModel> actual = ordersService.getOrders().block();

        assertEquals(List.of(), actual);
    }

    @Test
    void getOrder_WhenNotFound_ShouldThrowOrderNotFoundException() {
        when(orderR2dbcRepository.findById(1L)).thenReturn(Mono.empty());
        when(orderItemCountR2dbcRepository.findAllByOrderId(1L)).thenReturn(Flux.empty());
        assertThrows(OrderNotFoundException.class, () -> ordersService.getOrder(1L).block());
    }

    @Test
    void buy_WhenCartIsEmpty_ShouldThrowEmptyCartException() {
        when(cartItemCountR2dbcRepository.findAll()).thenReturn(Flux.empty());
        assertThrows(EmptyCartException.class, () -> ordersService.buy().block());
    }

    @Test
    void buy_ShouldCreateOrderItemsAndClearCart() {
        CartItemCountR2dbcEntity first = cartItem(1L, 2);
        CartItemCountR2dbcEntity second = cartItem(2L, 1);
        when(cartItemCountR2dbcRepository.findAll()).thenReturn(Flux.just(first, second));
        when(itemR2dbcRepository.findAllById(List.of(1L, 2L))).thenReturn(Flux.just(
            item(1L, "Apple", 100),
            item(2L, "Banana", 50)
        ));
        when(orderR2dbcRepository.save(any(OrderR2dbcEntity.class))).thenAnswer(invocation -> {
            OrderR2dbcEntity order = invocation.getArgument(0);
            order.setId(11L);
            return Mono.just(order);
        });
        when(orderItemCountR2dbcRepository.saveAll(any(Iterable.class))).thenReturn(Flux.empty());
        when(cartItemCountR2dbcRepository.deleteAll(List.of(first, second))).thenReturn(Mono.empty());

        Long orderId = ordersService.buy().block();

        assertEquals(11L, orderId);
        verify(orderR2dbcRepository).save(any(OrderR2dbcEntity.class));
        verify(orderItemCountR2dbcRepository).saveAll(any(Iterable.class));
        verify(cartItemCountR2dbcRepository).deleteAll(List.of(first, second));
    }

    @Test
    void getOrders_ShouldMapItemsForEachOrder() {
        OrderR2dbcEntity order = new OrderR2dbcEntity();
        order.setId(2L);
        order.setTotalSum(250);
        when(orderR2dbcRepository.findAllByOrderByIdDesc()).thenReturn(Flux.just(order));

        OrderItemCountR2dbcEntity orderItem = new OrderItemCountR2dbcEntity();
        orderItem.setOrderId(2L);
        orderItem.setItemId(1L);
        orderItem.setCount(2);
        when(orderItemCountR2dbcRepository.findAll()).thenReturn(Flux.just(orderItem));
        when(itemR2dbcRepository.findAllById(List.of(1L))).thenReturn(Flux.just(item(1L, "Apple", 100)));

        List<OrderModel> actual = ordersService.getOrders().block();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        OrderModel actualOrder = actual.getFirst();
        assertEquals(2L, actualOrder.id());
        assertEquals(250, actualOrder.totalSum());
        assertEquals(1, actualOrder.items().size());
        assertEquals("Apple", actualOrder.items().getFirst().title());
    }

    private static ItemR2dbcEntity item(long id, String title, long price) {
        ItemR2dbcEntity item = new ItemR2dbcEntity();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("desc");
        item.setPrice(price);
        item.setImageId(1L);
        return item;
    }

    private static CartItemCountR2dbcEntity cartItem(long itemId, int count) {
        CartItemCountR2dbcEntity cartItem = new CartItemCountR2dbcEntity();
        cartItem.setItemId(itemId);
        cartItem.setCount(count);
        return cartItem;
    }
}
