package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("images")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ImageR2dbcEntity {
    @Id
    private Long id;

    @Column("content_type")
    private String contentType;

    @Column("bytes")
    private byte[] bytes;
}
