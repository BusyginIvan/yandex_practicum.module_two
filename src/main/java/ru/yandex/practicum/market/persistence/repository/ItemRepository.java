package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> { }
