package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemsService {
    private static final int ITEMS_PER_ROW = 3;
    private static final ItemModel EMPTY_ITEM = new ItemModel(
        -1,
        "",
        "",
        0,
        1,
        0
    );

    private final ItemRepository itemRepository;
    private final CartItemCountRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;

    public ItemsService(
        ItemRepository itemRepository,
        CartItemCountRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper
    ) {
        this.itemRepository = itemRepository;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
    }

    @Transactional(readOnly = true)
    public ItemsPageModel getItems(
        String search,
        ItemSort sort,
        int pageNumber,
        int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSpringSort(sort));
        Specification<ItemEntity> specification = buildSearchSpecification(search);
        Page<ItemEntity> page = itemRepository.findAll(specification, pageable);

        List<Long> itemIds = page.stream().map(ItemEntity::getId).toList();
        Map<Long, Integer> counts = cartItemCountRepository.findAllById(itemIds).stream()
            .collect(Collectors.toMap(CartItemCountEntity::getItemId, CartItemCountEntity::getCount));

        List<ItemModel> items = page.stream()
            .map(item -> itemModelMapper.toItemModel(item, counts.getOrDefault(item.getId(), 0)))
            .toList();

        return new ItemsPageModel(
            toRows(items),
            new PagingModel(pageSize, pageNumber, page.hasPrevious(), page.hasNext())
        );
    }

    private static Specification<ItemEntity> buildSearchSpecification(String search) {
        String trimmedSearch = search == null ? "" : search.trim();
        if (trimmedSearch.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String pattern = "%" + trimmedSearch.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), pattern),
            cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    private static Sort toSpringSort(ItemSort sort) {
        return switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };
    }

    private static List<List<ItemModel>> toRows(List<ItemModel> items) {
        List<List<ItemModel>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            int end = Math.min(i + ITEMS_PER_ROW, items.size());
            List<ItemModel> row = new ArrayList<>(items.subList(i, end));
            while (row.size() < ITEMS_PER_ROW) {
                row.add(EMPTY_ITEM);
            }
            rows.add(row);
        }
        return rows;
    }
}
