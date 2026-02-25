package ru.yandex.practicum.market.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.ImageRepository;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.OrderRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.market.service")
public class ServiceTestConfiguration {

    @Bean
    public ItemRepository itemRepository() {
        return mock(ItemRepository.class);
    }

    @Bean
    public CartItemCountRepository cartItemCountRepository() {
        return mock(CartItemCountRepository.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return mock(OrderRepository.class);
    }

    @Bean
    public OrderItemCountRepository orderItemCountRepository() {
        return mock(OrderItemCountRepository.class);
    }

    @Bean
    public ImageRepository imageRepository() {
        return mock(ImageRepository.class);
    }
}
