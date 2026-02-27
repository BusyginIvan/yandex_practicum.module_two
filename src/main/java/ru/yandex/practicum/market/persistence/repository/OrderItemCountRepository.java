package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountEntity;
import ru.yandex.practicum.market.persistence.entity.OrderItemCountId;

import java.util.Collection;
import java.util.List;

public interface OrderItemCountRepository extends JpaRepository<OrderItemCountEntity, OrderItemCountId> {
    @Query("select oic from OrderItemCountEntity oic join fetch oic.item i where oic.order.id = :orderId order by i.id")
    List<OrderItemCountEntity> findAllByOrderIdWithItems(long orderId);

    @Query("select oic from OrderItemCountEntity oic join fetch oic.item i where oic.order.id in :orderIds order by oic.order.id desc, i.id")
    List<OrderItemCountEntity> findAllByOrderIdsWithItems(Collection<Long> orderIds);
}
