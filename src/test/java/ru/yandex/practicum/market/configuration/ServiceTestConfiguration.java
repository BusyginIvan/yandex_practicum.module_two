package ru.yandex.practicum.market.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderR2dbcRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.market.service")
public class ServiceTestConfiguration {

    @Bean
    public ItemR2dbcRepository itemR2dbcRepository() {
        return mock(ItemR2dbcRepository.class);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate() {
        return mock(R2dbcEntityTemplate.class);
    }

    @Bean
    public CartItemCountR2dbcRepository cartItemCountR2dbcRepository() {
        return mock(CartItemCountR2dbcRepository.class);
    }

    @Bean
    public OrderR2dbcRepository orderR2dbcRepository() {
        return mock(OrderR2dbcRepository.class);
    }

    @Bean
    public OrderItemCountR2dbcRepository orderItemCountR2dbcRepository() {
        return mock(OrderItemCountR2dbcRepository.class);
    }

    @Bean
    public ImageR2dbcRepository imageR2dbcRepository() {
        return mock(ImageR2dbcRepository.class);
    }
}
