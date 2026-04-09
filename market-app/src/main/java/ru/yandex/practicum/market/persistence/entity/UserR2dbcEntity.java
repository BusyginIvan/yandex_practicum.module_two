package ru.yandex.practicum.market.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserR2dbcEntity {
    @Id
    @Column("user_id")
    private Long userId;

    @Column("username")
    private String username;

    @Column("password")
    private String password;
}
