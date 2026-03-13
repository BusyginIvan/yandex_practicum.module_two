package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_item_counts")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class OrderItemCountR2dbcEntity {
    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Column("count")
    private int count;
}
