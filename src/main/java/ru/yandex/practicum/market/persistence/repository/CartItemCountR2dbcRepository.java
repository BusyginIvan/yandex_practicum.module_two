package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.CartItemCountR2dbcEntity;

public interface CartItemCountR2dbcRepository extends ReactiveCrudRepository<CartItemCountR2dbcEntity, Long> {
    default Mono<CartItemCountR2dbcEntity> create(CartItemCountR2dbcEntity entity) {
        entity.markNew();
        return save(entity);
    }

    default Mono<CartItemCountR2dbcEntity> update(CartItemCountR2dbcEntity entity) {
        entity.markPersisted();
        return save(entity);
    }
}
