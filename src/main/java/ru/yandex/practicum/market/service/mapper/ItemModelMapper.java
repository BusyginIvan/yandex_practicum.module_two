package ru.yandex.practicum.market.service.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

@Component
public class ItemModelMapper {
    public ItemModel toItemModel(ItemEntity item, int count) {
        return new ItemModel(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getPrice(),
            item.getImageId(),
            count
        );
    }

    public ItemModel toItemModel(ItemR2dbcEntity item, int count) {
        return new ItemModel(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getPrice(),
            item.getImageId(),
            count
        );
    }

    public ItemModel toItemModel(CartItemCountEntity cartItemCount) {
        return toItemModel(cartItemCount.getItem(), cartItemCount.getCount());
    }
}
