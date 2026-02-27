package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.exception.not_found.ItemNotFoundException;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final CartItemCountRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;

    public ItemService(
        ItemRepository itemRepository,
        CartItemCountRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper
    ) {
        this.itemRepository = itemRepository;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
    }

    @Transactional(readOnly = true)
    public ItemModel getItem(long id) {
        ItemEntity item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        int count = cartItemCountRepository.findById(id)
            .map(CartItemCountEntity::getCount)
            .orElse(0);
        return itemModelMapper.toItemModel(item, count);
    }

    @Transactional
    public ItemModel updateCartItemCount(long id, CartItemCountAction action) {
        ItemEntity item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        CartItemCountEntity cartItemCount = cartItemCountRepository.findById(id).orElse(null);

        int count = switch (action) {
            case PLUS -> incrementCartItemCount(item, cartItemCount);
            case MINUS -> decrementCartItemCount(cartItemCount);
            case DELETE -> deleteCartItemCount(cartItemCount);
        };

        return itemModelMapper.toItemModel(item, count);
    }

    private int incrementCartItemCount(ItemEntity item, CartItemCountEntity cartItemCount) {
        if (cartItemCount == null) {
            CartItemCountEntity newCartItemCount = new CartItemCountEntity();
            newCartItemCount.setItem(item);
            newCartItemCount.setCount(1);
            cartItemCountRepository.save(newCartItemCount);
            return 1;
        }

        int newCount = cartItemCount.getCount() + 1;
        cartItemCount.setCount(newCount);
        cartItemCountRepository.save(cartItemCount);
        return newCount;
    }

    private int decrementCartItemCount(CartItemCountEntity cartItemCount) {
        if (cartItemCount == null) return 0;

        int newCount = cartItemCount.getCount() - 1;
        if (newCount <= 0) {
            cartItemCountRepository.delete(cartItemCount);
            return 0;
        }

        cartItemCount.setCount(newCount);
        cartItemCountRepository.save(cartItemCount);
        return newCount;
    }

    @SuppressWarnings("SameReturnValue")
    private int deleteCartItemCount(CartItemCountEntity cartItemCount) {
        if (cartItemCount == null) return 0;
        cartItemCountRepository.delete(cartItemCount);
        return 0;
    }
}
