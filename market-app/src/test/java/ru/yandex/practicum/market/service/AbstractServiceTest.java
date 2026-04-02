package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.market.configuration.ServiceTestConfiguration;
import ru.yandex.practicum.market.integration.payment.PaymentClient;
import ru.yandex.practicum.market.persistence.repository.CartItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderItemCountR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.OrderR2dbcRepository;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = ServiceTestConfiguration.class)
public abstract class AbstractServiceTest {

    @Autowired protected ItemR2dbcRepository itemR2dbcRepository;
    @Autowired protected OrderR2dbcRepository orderR2dbcRepository;
    @Autowired protected CartItemCountR2dbcRepository cartItemCountR2dbcRepository;
    @Autowired protected OrderItemCountR2dbcRepository orderItemCountR2dbcRepository;
    @Autowired protected ImageR2dbcRepository imageR2dbcRepository;
    @Autowired protected R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired protected PaymentClient paymentClient;

    @BeforeEach
    void resetMocks() {
        reset(
            itemR2dbcRepository,
            orderR2dbcRepository,
            cartItemCountR2dbcRepository,
            orderItemCountR2dbcRepository,
            imageR2dbcRepository,
            r2dbcEntityTemplate,
            paymentClient
        );
    }
}
