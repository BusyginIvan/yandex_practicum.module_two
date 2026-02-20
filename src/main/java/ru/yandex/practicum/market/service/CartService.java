package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.market.api.model.CartModel;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

import java.util.List;

@Service
public class CartService {
    private final ItemService itemService;
    private final CartItemCountRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;

    public CartService(
        ItemService itemService,
        CartItemCountRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper
    ) {
        this.itemService = itemService;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
    }

    @Transactional(readOnly = true)
    public CartModel getCart() {
        return loadCart();
    }

    @Transactional
    public CartModel changeItemCount(long itemId, CartItemCountAction action) {
        itemService.updateCartItemCount(itemId, action);
        return loadCart();
    }

    private CartModel loadCart() {
        List<CartItemCountEntity> cartItems = cartItemCountRepository.findAllWithItems();
        List<ItemModel> itemModels = cartItems.stream().map(itemModelMapper::toItemModel).toList();
        long total = cartItems.stream().mapToLong(CartItemCountEntity::getSubtotal).sum();
        return new CartModel(itemModels, total);
    }
}
