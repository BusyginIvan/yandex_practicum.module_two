package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

public interface ItemR2dbcRepository extends ReactiveCrudRepository<ItemR2dbcEntity, Long> { }
