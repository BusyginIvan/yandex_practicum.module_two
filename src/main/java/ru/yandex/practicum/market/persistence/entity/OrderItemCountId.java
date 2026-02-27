package ru.yandex.practicum.market.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItemCountId implements Serializable {
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;
}
