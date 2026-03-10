package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;

public interface ImageR2dbcRepository extends ReactiveCrudRepository<ImageR2dbcEntity, Long> { }
