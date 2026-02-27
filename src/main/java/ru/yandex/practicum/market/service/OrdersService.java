package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.market.api.model.OrderItemModel;
import ru.yandex.practicum.market.api.model.OrderModel;
import ru.yandex.practicum.market.exception.not_found.OrderNotFoundException;
import ru.yandex.practicum.market.exception.validation.EmptyCartException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.entity.OrderEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountId;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrdersService {
    private final OrderRepository orderRepository;
    private final OrderItemCountRepository orderItemCountRepository;
    private final CartItemCountRepository cartItemCountRepository;

    public OrdersService(
        OrderRepository orderRepository,
        OrderItemCountRepository orderItemCountRepository,
        CartItemCountRepository cartItemCountRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemCountRepository = orderItemCountRepository;
        this.cartItemCountRepository = cartItemCountRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderModel> getOrders() {
        List<OrderEntity> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (orders.isEmpty()) return List.of();
        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();

        Map<Long, List<OrderItemCountEntity>> itemsByOrderId = orderItemCountRepository
            .findAllByOrderIdsWithItems(orderIds)
            .stream()
            .collect(Collectors.groupingBy(OrderItemCountEntity::getOrderId));

        return orders.stream()
            .map(order -> toOrderModel(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderModel getOrder(long id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        List<OrderItemCountEntity> orderItems = orderItemCountRepository.findAllByOrderIdWithItems(id);
        return toOrderModel(order, orderItems);
    }

    @Transactional
    public long buy() {
        List<CartItemCountEntity> cartItems = cartItemCountRepository.findAllWithItems();
        if (cartItems.isEmpty()) throw new EmptyCartException();

        long totalSum = cartItems.stream().mapToLong(CartItemCountEntity::getSubtotal).sum();

        OrderEntity order = new OrderEntity();
        order.setTotalSum(totalSum);
        orderRepository.save(order);

        List<OrderItemCountEntity> orderItems = cartItems.stream()
            .map(cartItem -> toOrderItemCount(order, cartItem))
            .toList();
        orderItemCountRepository.saveAll(orderItems);

        cartItemCountRepository.deleteAllInBatch(cartItems);

        return order.getId();
    }

    private static OrderItemCountEntity toOrderItemCount(OrderEntity order, CartItemCountEntity cartItem) {
        ItemEntity item = cartItem.getItem();
        OrderItemCountEntity orderItem = new OrderItemCountEntity();
        orderItem.setId(new OrderItemCountId(order.getId(), item.getId()));
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setCount(cartItem.getCount());
        return orderItem;
    }

    private static OrderModel toOrderModel(OrderEntity order, List<OrderItemCountEntity> orderItems) {
        List<OrderItemModel> items = orderItems.stream()
            .map(OrdersService::toOrderItemModel)
            .toList();
        return new OrderModel(order.getId(), items, order.getTotalSum());
    }

    private static OrderItemModel toOrderItemModel(OrderItemCountEntity orderItem) {
        ItemEntity item = orderItem.getItem();
        return new OrderItemModel(
            item.getId(),
            item.getTitle(),
            item.getPrice(),
            orderItem.getCount()
        );
    }
}
