package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.UserR2dbcEntity;

public interface UserR2dbcRepository extends ReactiveCrudRepository<UserR2dbcEntity, Long> {
    Mono<UserR2dbcEntity> findByUsername(String username);
    Mono<Boolean> existsByUsername(String username);
}
