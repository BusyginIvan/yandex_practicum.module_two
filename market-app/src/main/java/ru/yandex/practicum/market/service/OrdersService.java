package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.OrderItemModel;
import ru.yandex.practicum.market.api.model.OrderModel;
import ru.yandex.practicum.market.exception.not_found.OrderNotFoundException;
import ru.yandex.practicum.market.exception.validation.EmptyCartException;
import ru.yandex.practicum.market.integration.payment.PaymentClient;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.OrderR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderR2dbcRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrdersService {
    private final OrderR2dbcRepository orderRepository;
    private final OrderItemCountR2dbcRepository orderItemCountRepository;
    private final ItemR2dbcRepository itemRepository;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final PaymentClient paymentClient;
    private final CurrentUserService currentUserService;

    public OrdersService(
        OrderR2dbcRepository orderRepository,
        OrderItemCountR2dbcRepository orderItemCountRepository,
        ItemR2dbcRepository itemRepository,
        CartItemCountR2dbcRepository cartItemCountRepository,
        PaymentClient paymentClient,
        CurrentUserService currentUserService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemCountRepository = orderItemCountRepository;
        this.itemRepository = itemRepository;
        this.cartItemCountRepository = cartItemCountRepository;
        this.paymentClient = paymentClient;
        this.currentUserService = currentUserService;
    }

    public Mono<List<OrderModel>> getOrders() {
        return currentUserService.getCurrentUserId()
            .flatMap(userId -> orderRepository.findAllByUserIdOrderByIdDesc(userId).collectList())
            .flatMap(orders -> {
                if (orders.isEmpty()) return Mono.just(List.of());

                List<Long> orderIds = orders.stream().map(OrderR2dbcEntity::getId).toList();
                return orderItemCountRepository.findAllByOrderIdIn(orderIds).collectList().flatMap(itemCounts -> {
                    Map<Long, List<OrderItemCountR2dbcEntity>> itemCountsByOrderId = itemCounts.stream()
                        .collect(Collectors.groupingBy(OrderItemCountR2dbcEntity::getOrderId));
                    List<Long> itemIds = itemCounts.stream().map(OrderItemCountR2dbcEntity::getItemId).distinct().toList();
                    return itemRepository.findAllById(itemIds)
                        .collectMap(ItemR2dbcEntity::getId, Function.identity())
                        .map(itemByItemId -> buildOrderModels(orders, itemCountsByOrderId, itemByItemId));
                });
            });
    }

    public Mono<OrderModel> getOrder(long id) {
        return currentUserService.getCurrentUserId().flatMap(userId -> {
            Mono<OrderR2dbcEntity> orderMono = orderRepository.findByIdAndUserId(id, userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)));
            Mono<List<OrderItemCountR2dbcEntity>> itemCountsMono =
                orderItemCountRepository.findAllByOrderId(id).collectList();
            return Mono.zip(orderMono, itemCountsMono).flatMap(tuple -> {
                OrderR2dbcEntity order = tuple.getT1();
                List<OrderItemCountR2dbcEntity> itemCounts = tuple.getT2();
                List<Long> itemIds = itemCounts.stream().map(OrderItemCountR2dbcEntity::getItemId).toList();
                return itemRepository.findAllById(itemIds)
                    .collectMap(ItemR2dbcEntity::getId, Function.identity())
                    .map(itemByItemId -> buildOrderModel(order, itemCounts, itemByItemId));
            });
        });
    }

    private List<OrderModel> buildOrderModels(
        List<OrderR2dbcEntity> orders,
        Map<Long, List<OrderItemCountR2dbcEntity>> itemCountsByOrderId,
        Map<Long, ItemR2dbcEntity> itemByItemId
    ) {
        return orders.stream().map(order -> buildOrderModel(
            order,
            itemCountsByOrderId.getOrDefault(order.getId(), List.of()),
            itemByItemId
        )).toList();
    }

    private OrderModel buildOrderModel(
        OrderR2dbcEntity order,
        List<OrderItemCountR2dbcEntity> itemCounts,
        Map<Long, ItemR2dbcEntity> itemByItemId
    ) {
        return new OrderModel(
            order.getId(),
            itemCounts.stream().map(itemCount -> {
                ItemR2dbcEntity item = itemByItemId.get(itemCount.getItemId());
                return new OrderItemModel(
                    item.getId(),
                    item.getTitle(),
                    item.getPrice(),
                    itemCount.getCount()
                );
            }).toList(),
            order.getTotalSum()
        );
    }

    @Transactional
    public Mono<Long> buy() {
        return currentUserService.getCurrentUserId().flatMap(userId ->
            cartItemCountRepository.findAllByUserId(userId).collectList().flatMap(cartItems -> {
                if (cartItems.isEmpty()) return Mono.error(new EmptyCartException());

                List<Long> itemIds = cartItems.stream()
                    .map(CartItemCountR2dbcEntity::getItemId)
                    .toList();

                return itemRepository.findAllById(itemIds)
                    .collectMap(ItemR2dbcEntity::getId, Function.identity())
                    .flatMap(itemById -> buy(userId, cartItems, itemById));
            }));
    }

    private Mono<Long> buy(
        long userId,
        List<CartItemCountR2dbcEntity> cartItems,
        Map<Long, ItemR2dbcEntity> itemById
    ) {
        long totalSum = cartItems.stream().mapToLong(cartItem -> {
            ItemR2dbcEntity item = itemById.get(cartItem.getItemId());
            return item.getPrice() * cartItem.getCount();
        }).sum();

        return paymentClient.makePayment(totalSum)
            .flatMap(ignored -> createOrder(userId, cartItems, totalSum));
    }

    private Mono<Long> createOrder(long userId, List<CartItemCountR2dbcEntity> cartItems, long totalSum) {
        OrderR2dbcEntity order = new OrderR2dbcEntity();
        order.setUserId(userId);
        order.setTotalSum(totalSum);
        return orderRepository.save(order).flatMap(savedOrder -> {
            List<OrderItemCountR2dbcEntity> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItemCountR2dbcEntity orderItem = new OrderItemCountR2dbcEntity();
                    orderItem.setOrderId(savedOrder.getId());
                    orderItem.setItemId(cartItem.getItemId());
                    orderItem.setCount(cartItem.getCount());
                    return orderItem;
                })
                .toList();

            return orderItemCountRepository.saveAll(orderItems)
                .then(cartItemCountRepository.deleteAllByUserId(userId))
                .thenReturn(savedOrder.getId());
        });
    }
}
