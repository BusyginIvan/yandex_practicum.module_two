package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

public interface ItemRepository extends
    JpaRepository<ItemEntity, Long>,
    JpaSpecificationExecutor<ItemEntity> { }
