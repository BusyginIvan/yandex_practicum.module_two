package ru.yandex.practicum.market.integration.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.client.payment.api.PaymentApi;
import ru.yandex.practicum.client.payment.invoker.ApiClient;

@Configuration
public class PaymentClientConfig {
    @Bean
    public ApiClient paymentApiClient(@Value("${payment.base-url}") String baseUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }
}
