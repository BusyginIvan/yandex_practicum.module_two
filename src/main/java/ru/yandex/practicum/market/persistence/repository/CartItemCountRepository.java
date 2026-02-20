package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.market.persistence.entity.CartItemCountEntity;

import java.util.List;

public interface CartItemCountRepository extends JpaRepository<CartItemCountEntity, Long> {
    @Query("select c from CartItemCountEntity c join fetch c.item i order by i.id")
    List<CartItemCountEntity> findAllWithItems();
}
