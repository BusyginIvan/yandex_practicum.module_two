package ru.yandex.practicum.market.integration.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.client.payment.api.PaymentApi;
import ru.yandex.practicum.client.payment.invoker.ApiClient;

@Configuration
public class PaymentClientConfig {
    private static final String CLIENT_REGISTRATION_ID = "market";
    private static final String CLIENT_PRINCIPAL = "market-app";

    @Bean
    public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(
        ReactiveClientRegistrationRepository clientRegistrationRepository,
        ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
            new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
            );

        manager.setAuthorizedClientProvider(
            ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build()
        );

        return manager;
    }

    @Bean
    public WebClient paymentWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return ApiClient.buildWebClientBuilder()
            .filter(bearerTokenFilter(authorizedClientManager))
            .build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient paymentWebClient, @Value("${payment.base-url}") String baseUrl) {
        ApiClient apiClient = new ApiClient(paymentWebClient);
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }

    private ExchangeFilterFunction bearerTokenFilter(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        return (request, next) -> authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_REGISTRATION_ID)
                    .principal(CLIENT_PRINCIPAL)
                    .build()
            )
            .switchIfEmpty(Mono.error(new IllegalStateException("OAuth2 token was not acquired for payment-service")))
            .map(authorizedClient -> authorizedClient.getAccessToken().getTokenValue())
            .flatMap(accessToken -> next.exchange(
                ClientRequest.from(request)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .build()
            ));
    }
}
