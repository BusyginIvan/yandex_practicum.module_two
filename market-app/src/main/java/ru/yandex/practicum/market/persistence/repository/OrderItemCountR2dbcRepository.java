package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountR2dbcEntity;

public interface OrderItemCountR2dbcRepository extends ReactiveCrudRepository<OrderItemCountR2dbcEntity, Long> {
    Flux<OrderItemCountR2dbcEntity> findAllByOrderId(long id);
}