package ru.yandex.practicum.market.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.service.mapper.ItemModelMapper;

import java.util.ArrayList;
import java.util.List;

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

    private final R2dbcEntityTemplate entityTemplate;
    private final CartItemCountR2dbcRepository cartItemCountRepository;
    private final ItemModelMapper itemModelMapper;

    public ItemsService(
        R2dbcEntityTemplate entityTemplate,
        CartItemCountR2dbcRepository cartItemCountRepository,
        ItemModelMapper itemModelMapper
    ) {
        this.entityTemplate = entityTemplate;
        this.cartItemCountRepository = cartItemCountRepository;
        this.itemModelMapper = itemModelMapper;
    }

    public Mono<ItemsPageModel> getItems(
        String search,
        ItemSort sort,
        int pageNumber,
        int pageSize
    ) {
        Criteria criteria = toSearchCriteria(search);
        int offset = (pageNumber - 1) * pageSize;

        Query countQuery = Query.query(criteria);
        Mono<Long> totalCountMono = entityTemplate.count(countQuery, ItemR2dbcEntity.class);

        Sort sortSpec = toSpringSort(sort);
        Query selectQuery = Query.query(criteria).sort(sortSpec).limit(pageSize).offset(offset);
        Mono<List<ItemR2dbcEntity>> itemsMono =
            entityTemplate.select(selectQuery, ItemR2dbcEntity.class).collectList();

        return Mono.zip(itemsMono, totalCountMono).flatMap(tuple -> {
            List<ItemR2dbcEntity> items = tuple.getT1();
            long totalCount = tuple.getT2();
            boolean hasPrevious = pageNumber > 1;
            long totalPages = totalCount == 0 ? 0 : (totalCount - 1) / pageSize + 1;
            boolean hasNext = pageNumber < totalPages;

            if (items.isEmpty()) {
                return Mono.just(new ItemsPageModel(
                    List.of(),
                    new PagingModel(pageSize, pageNumber, hasPrevious, hasNext)
                ));
            }

            List<Long> itemIds = items.stream().map(ItemR2dbcEntity::getId).toList();
            return cartItemCountRepository.findAllById(itemIds)
                .collectMap(CartItemCountR2dbcEntity::getItemId, CartItemCountR2dbcEntity::getCount)
                .map(counts -> {
                    List<ItemModel> itemModels = items.stream()
                        .map(item ->
                            itemModelMapper.toItemModel(item, counts.getOrDefault(item.getId(), 0))
                        )
                        .toList();
                    return new ItemsPageModel(
                        toRows(itemModels),
                        new PagingModel(pageSize, pageNumber, hasPrevious, hasNext)
                    );
                });
        });
    }

    private static Criteria toSearchCriteria(String search) {
        String trimmedSearch = search == null ? "" : search.trim();
        if (trimmedSearch.isEmpty()) return Criteria.empty();
        String pattern = "%" + trimmedSearch + "%";
        return Criteria.where("title").like(pattern).ignoreCase(true)
            .or(Criteria.where("description").like(pattern).ignoreCase(true));
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
