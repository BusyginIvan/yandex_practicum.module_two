package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class OrderR2dbcEntity {
    @Id
    private Long id;

    @Column("total_sum")
    private long totalSum;
}
