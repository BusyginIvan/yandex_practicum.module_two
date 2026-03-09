package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.market.persistence.entity.OrderR2dbcEntity;

public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderR2dbcEntity, Long> {
    Flux<OrderR2dbcEntity> findAllByOrderByIdDesc();
}