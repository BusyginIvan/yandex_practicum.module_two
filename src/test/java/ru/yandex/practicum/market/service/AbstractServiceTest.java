package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.market.configuration.ServiceTestConfiguration;
import ru.yandex.practicum.market.persistence.repository.CartItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.ImageRepository;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountRepository;
import ru.yandex.practicum.market.persistence.repository.OrderRepository;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = ServiceTestConfiguration.class)
public abstract class AbstractServiceTest {

    @Autowired protected ItemRepository itemRepository;
    @Autowired protected CartItemCountRepository cartItemCountRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderItemCountRepository orderItemCountRepository;
    @Autowired protected ImageRepository imageRepository;

    @BeforeEach
    void resetMocks() {
        reset(
            itemRepository,
            cartItemCountRepository,
            orderRepository,
            orderItemCountRepository,
            imageRepository
        );
    }
}
