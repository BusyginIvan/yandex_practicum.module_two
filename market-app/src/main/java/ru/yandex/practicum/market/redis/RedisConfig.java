package ru.yandex.practicum.market.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisTemplate<String, ItemR2dbcEntity> itemRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
    ) {
        return buildTemplate(connectionFactory, objectMapper, ItemR2dbcEntity.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, ItemsPageCache> itemsPageRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
    ) {
        return buildTemplate(connectionFactory, objectMapper, ItemsPageCache.class);
    }

    private static <T> ReactiveRedisTemplate<String, T> buildTemplate(
        ReactiveRedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper,
        Class<T> valueType
    ) {
        Jackson2JsonRedisSerializer<T> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, valueType);

        RedisSerializationContext<String, T> context =
            RedisSerializationContext.<String, T>newSerializationContext(new StringRedisSerializer())
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
