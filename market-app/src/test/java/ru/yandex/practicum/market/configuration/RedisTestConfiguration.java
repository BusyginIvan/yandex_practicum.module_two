package ru.yandex.practicum.market.configuration;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

@TestConfiguration
public class RedisTestConfiguration {

    private static final int REDIS_PORT = 6379;

    @SuppressWarnings("resource")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7.2").withExposedPorts(REDIS_PORT);
    }

    @Bean
    public static BeanFactoryPostProcessor redisProps(GenericContainer<?> redis) {
        return bf -> {
            ConfigurableEnvironment env = bf.getBean(ConfigurableEnvironment.class);

            Map<String, Object> props = Map.of(
                "spring.data.redis.host", redis.getHost(),
                "spring.data.redis.port", redis.getMappedPort(REDIS_PORT)
            );

            env.getPropertySources().addFirst(new MapPropertySource("testcontainers-redis", props));
        };
    }
}
