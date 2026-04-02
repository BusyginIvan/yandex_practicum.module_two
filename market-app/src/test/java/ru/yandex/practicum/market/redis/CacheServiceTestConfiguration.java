package ru.yandex.practicum.market.redis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.market.redis")
public class CacheServiceTestConfiguration {

    @Bean
    public ItemR2dbcRepository itemR2dbcRepository() {
        return mock(ItemR2dbcRepository.class);
    }

    @Bean
    public ImageR2dbcRepository imageR2dbcRepository() {
        return mock(ImageR2dbcRepository.class);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate() {
        return mock(R2dbcEntityTemplate.class);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ReactiveRedisTemplate<String, ItemR2dbcEntity> itemRedisTemplate() {
        return mock(ReactiveRedisTemplate.class);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ReactiveRedisTemplate<String, ItemsPageCache> itemsPageRedisTemplate() {
        return mock(ReactiveRedisTemplate.class);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public ReactiveRedisTemplate<String, byte[]> imageHashRedisTemplate() {
        return mock(ReactiveRedisTemplate.class);
    }
}
