package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;

import java.util.Collection;

public interface CartItemCountR2dbcRepository extends ReactiveCrudRepository<CartItemCountR2dbcEntity, Long> {
    Flux<CartItemCountR2dbcEntity> findAllByUserId(Long userId);

    Flux<CartItemCountR2dbcEntity> findAllByUserIdAndItemIdIn(Long userId, Collection<Long> itemIds);

    Mono<CartItemCountR2dbcEntity> findByUserIdAndItemId(Long userId, Long itemId);

    @Modifying
    @Query("INSERT INTO cart_item_counts (user_id, item_id, count) VALUES (:userId, :itemId, :count)")
    Mono<Integer> insert(
        @Param("userId") Long userId,
        @Param("itemId") Long itemId,
        @Param("count") int count
    );

    @Modifying
    @Query("UPDATE cart_item_counts SET count = :count WHERE user_id = :userId AND item_id = :itemId")
    Mono<Integer> updateCount(
        @Param("userId") Long userId,
        @Param("itemId") Long itemId,
        @Param("count") int count
    );

    Mono<Void> deleteByUserIdAndItemId(Long userId, Long itemId);

    Mono<Void> deleteAllByUserId(Long userId);

    default Mono<CartItemCountR2dbcEntity> create(CartItemCountR2dbcEntity entity) {
        return insert(entity.getUserId(), entity.getItemId(), entity.getCount()).thenReturn(entity);
    }

    default Mono<CartItemCountR2dbcEntity> update(CartItemCountR2dbcEntity entity) {
        return updateCount(entity.getUserId(), entity.getItemId(), entity.getCount()).thenReturn(entity);
    }
}
