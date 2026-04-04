package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("items")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ItemR2dbcEntity {
    @Id
    private Long id;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("price")
    private long price;

    @Column("image_id")
    private long imageId;
}
