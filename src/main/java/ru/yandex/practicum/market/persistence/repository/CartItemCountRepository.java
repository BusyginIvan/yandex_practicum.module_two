package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;

public interface CartItemCountRepository extends JpaRepository<CartItemCountEntity, Long> { }
