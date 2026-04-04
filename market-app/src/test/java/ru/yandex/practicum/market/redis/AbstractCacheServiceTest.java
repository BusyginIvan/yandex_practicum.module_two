package ru.yandex.practicum.market.redis;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = CacheServiceTestConfiguration.class)
public abstract class AbstractCacheServiceTest {

    @Autowired protected ItemR2dbcRepository itemR2dbcRepository;
    @Autowired protected ImageR2dbcRepository imageR2dbcRepository;
    @Autowired protected R2dbcEntityTemplate r2dbcEntityTemplate;

    @Autowired protected ReactiveRedisTemplate<String, ItemR2dbcEntity> itemRedisTemplate;
    @Autowired protected ReactiveRedisTemplate<String, ItemsPageCache> itemsPageRedisTemplate;
    @Autowired protected ReactiveRedisTemplate<String, byte[]> imageHashRedisTemplate;

    @BeforeEach
    void resetMocks() {
        reset(
            itemR2dbcRepository,
            imageR2dbcRepository,
            r2dbcEntityTemplate,
            itemRedisTemplate,
            itemsPageRedisTemplate,
            imageHashRedisTemplate
        );
    }
}
