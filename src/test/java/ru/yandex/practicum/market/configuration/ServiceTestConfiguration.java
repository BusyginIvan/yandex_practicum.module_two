package ru.yandex.practicum.market.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.ImageRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderRepository;
import ru.yandex.practicum.market.persistence.repository.OrderR2dbcRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.market.service")
public class ServiceTestConfiguration {

    @Bean
    public ItemRepository itemRepository() {
        return mock(ItemRepository.class);
    }

    @Bean
    public ItemR2dbcRepository itemR2dbcRepository() {
        return mock(ItemR2dbcRepository.class);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate() {
        return mock(R2dbcEntityTemplate.class);
    }

    @Bean
    public CartItemCountRepository cartItemCountRepository() {
        return mock(CartItemCountRepository.class);
    }

    @Bean
    public CartItemCountR2dbcRepository cartItemCountR2dbcRepository() {
        return mock(CartItemCountR2dbcRepository.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return mock(OrderRepository.class);
    }

    @Bean
    public OrderR2dbcRepository orderR2dbcRepository() {
        return mock(OrderR2dbcRepository.class);
    }

    @Bean
    public OrderItemCountRepository orderItemCountRepository() {
        return mock(OrderItemCountRepository.class);
    }

    @Bean
    public OrderItemCountR2dbcRepository orderItemCountR2dbcRepository() {
        return mock(OrderItemCountR2dbcRepository.class);
    }

    @Bean
    public ImageRepository imageRepository() {
        return mock(ImageRepository.class);
    }
}
