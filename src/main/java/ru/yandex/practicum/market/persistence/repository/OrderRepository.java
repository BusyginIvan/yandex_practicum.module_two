package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.persistence.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> { }
