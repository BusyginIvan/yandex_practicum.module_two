package ru.yandex.practicum.market.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.market.integration.payment.PaymentClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class PaymentClientTestConfiguration {
    @Primary @Bean
    public PaymentClient paymentClient() {
        return mock(PaymentClient.class);
    }
}
