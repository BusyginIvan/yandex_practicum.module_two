package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.CartModel;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.redis.ItemCacheService;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final ItemService itemService;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final ItemCacheService itemCacheService;
    private final ItemModelMapper itemModelMapper;

    public CartService(
        ItemService itemService,
        CartItemCountR2dbcRepository cartItemCountRepository,
        ItemCacheService itemCacheService,
        ItemModelMapper itemModelMapper
    ) {
        this.itemService = itemService;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemCacheService = itemCacheService;
        this.itemModelMapper = itemModelMapper;
    }

    public Mono<CartModel> getCart() {
        return loadCart();
    }

    public Mono<CartModel> changeItemCount(long itemId, CartItemCountAction action) {
        return itemService.updateCartItemCount(itemId, action).then(loadCart());
    }

    private Mono<CartModel> loadCart() {
        return cartItemCountRepository.findAll().collectList().flatMap(cartItems -> {
            if (cartItems.isEmpty()) return Mono.just(new CartModel(List.of(), 0));

            List<Long> itemIds = cartItems.stream().map(CartItemCountR2dbcEntity::getItemId).toList();
            Map<Long, Integer> counts = cartItems.stream()
                .collect(Collectors.toMap(CartItemCountR2dbcEntity::getItemId, CartItemCountR2dbcEntity::getCount));

            return itemCacheService.getByIds(itemIds).map(items -> {
                List<ItemModel> itemModels = items.stream()
                    .map(item -> itemModelMapper.toItemModel(item, counts.get(item.getId())))
                    .sorted(Comparator.comparing(ItemModel::id))
                    .toList();
                long total = itemModels.stream().mapToLong(ItemModel::getSubtotal).sum();
                return new CartModel(itemModels, total);
            });
        });
    }
}
