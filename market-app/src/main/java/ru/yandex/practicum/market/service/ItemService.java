package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.exception.not_found.ItemNotFoundException;
import ru.yandex.practicum.market.redis.ItemCacheService;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

@Service
public class ItemService {
    private final ItemCacheService itemCacheService;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;
    private final CurrentUserService currentUserService;

    public ItemService(
        ItemCacheService itemCacheService,
        CartItemCountR2dbcRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper,
        CurrentUserService currentUserService
    ) {
        this.itemCacheService = itemCacheService;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
        this.currentUserService = currentUserService;
    }

    public Mono<ItemModel> getItem(long id) {
        return itemCacheService.getById(id)
            .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
            .flatMap(item -> currentUserService.getCurrentUserIdOrEmpty()
                .flatMap(userId -> cartItemCountRepository.findByUserIdAndItemId(userId, id)
                    .map(CartItemCountR2dbcEntity::getCount))
                .defaultIfEmpty(0)
                .map(count -> itemModelMapper.toItemModel(item, count)));
    }

    public Mono<ItemModel> updateCartItemCount(long id, CartItemCountAction action) {
        return itemCacheService.getById(id)
            .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
            .flatMap(item -> {
                Mono<Integer> countMono = switch (action) {
                    case PLUS -> incrementCartItemCount(id);
                    case MINUS -> decrementCartItemCount(id);
                    case DELETE -> deleteCartItemCount(id);
                };
                return countMono.map(count -> itemModelMapper.toItemModel(item, count));
            });
    }

    private Mono<Integer> incrementCartItemCount(long itemId) {
        return currentUserService.getCurrentUserId().flatMap(userId ->
            cartItemCountRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(cartItemCount -> {
                    int newCount = cartItemCount.getCount() + 1;
                    cartItemCount.setCount(newCount);
                    return cartItemCountRepository.update(cartItemCount).thenReturn(newCount);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CartItemCountR2dbcEntity newCartItemCount = new CartItemCountR2dbcEntity();
                    newCartItemCount.setUserId(userId);
                    newCartItemCount.setItemId(itemId);
                    newCartItemCount.setCount(1);
                    return cartItemCountRepository.create(newCartItemCount).thenReturn(1);
                })));
    }

    private Mono<Integer> decrementCartItemCount(long itemId) {
        return currentUserService.getCurrentUserId().flatMap(userId ->
            cartItemCountRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(cartItemCount -> {
                    int newCount = cartItemCount.getCount() - 1;
                    if (newCount <= 0) {
                        return cartItemCountRepository.deleteByUserIdAndItemId(userId, itemId).thenReturn(0);
                    }
                    cartItemCount.setCount(newCount);
                    return cartItemCountRepository.update(cartItemCount).thenReturn(newCount);
                })
                .defaultIfEmpty(0));
    }

    private Mono<Integer> deleteCartItemCount(long itemId) {
        return currentUserService.getCurrentUserId()
            .flatMap(userId -> cartItemCountRepository.deleteByUserIdAndItemId(userId, itemId))
            .thenReturn(0);
    }
}
