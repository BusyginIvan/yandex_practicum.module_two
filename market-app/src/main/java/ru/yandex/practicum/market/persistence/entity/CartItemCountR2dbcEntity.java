package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_item_counts")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CartItemCountR2dbcEntity implements Persistable<Long> {
    @Id
    @Column("item_id")
    private Long itemId;

    @Column("user_id")
    private Long userId;

    @Column("count")
    private int count;

    @Transient
    private boolean isNew;

    @Override
    public Long getId() {
        return itemId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNew() {
        this.isNew = true;
    }

    public void markPersisted() {
        this.isNew = false;
    }
}
