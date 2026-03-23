package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.exception.not_found.ItemNotFoundException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

@Service
public class ItemService {
    private final ItemR2dbcRepository itemRepository;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;

    public ItemService(
        ItemR2dbcRepository itemRepository,
        CartItemCountR2dbcRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper
    ) {
        this.itemRepository = itemRepository;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
    }

    public Mono<ItemModel> getItem(long id) {
        return itemRepository.findById(id)
            .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
            .flatMap(item -> cartItemCountRepository.findById(id)
                .map(CartItemCountR2dbcEntity::getCount)
                .defaultIfEmpty(0)
                .map(count -> itemModelMapper.toItemModel(item, count))
            );
    }

    public Mono<ItemModel> updateCartItemCount(long id, CartItemCountAction action) {
        return itemRepository.findById(id)
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
        return cartItemCountRepository.findById(itemId)
            .flatMap(cartItemCount -> {
                int newCount = cartItemCount.getCount() + 1;
                cartItemCount.setCount(newCount);
                return cartItemCountRepository.update(cartItemCount).thenReturn(newCount);
            })
            .switchIfEmpty(Mono.defer(() -> {
                CartItemCountR2dbcEntity newCartItemCount = new CartItemCountR2dbcEntity();
                newCartItemCount.setItemId(itemId);
                newCartItemCount.setCount(1);
                return cartItemCountRepository.create(newCartItemCount).thenReturn(1);
            }));
    }

    private Mono<Integer> decrementCartItemCount(long itemId) {
        return cartItemCountRepository.findById(itemId)
            .flatMap(cartItemCount -> {
                int newCount = cartItemCount.getCount() - 1;
                if (newCount <= 0) {
                    return cartItemCountRepository.deleteById(itemId).thenReturn(0);
                }
                cartItemCount.setCount(newCount);
                return cartItemCountRepository.update(cartItemCount).thenReturn(newCount);
            })
            .defaultIfEmpty(0);
    }

    private Mono<Integer> deleteCartItemCount(long itemId) {
        return cartItemCountRepository.deleteById(itemId).thenReturn(0);
    }
}
