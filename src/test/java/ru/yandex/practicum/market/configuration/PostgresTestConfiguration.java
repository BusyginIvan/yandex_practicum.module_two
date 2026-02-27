package ru.yandex.practicum.market.configuration;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@TestConfiguration
public class PostgresTestConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16");
    }

    @Bean
    public static BeanFactoryPostProcessor dbProps(PostgreSQLContainer<?> pg) {
        return bf -> {
            ConfigurableEnvironment env = bf.getBean(ConfigurableEnvironment.class);

            Map<String, Object> props = Map.of(
                "spring.datasource.url", pg.getJdbcUrl(),
                "spring.datasource.username", pg.getUsername(),
                "spring.datasource.password", pg.getPassword()
            );

            env.getPropertySources().addFirst(new MapPropertySource("testcontainers", props));
        };
    }
}
