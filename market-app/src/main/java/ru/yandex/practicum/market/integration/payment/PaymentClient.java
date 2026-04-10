package ru.yandex.practicum.market.integration.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.client.payment.api.PaymentApi;
import ru.yandex.practicum.client.payment.model.ErrorResponse;
import ru.yandex.practicum.client.payment.model.PaymentRequest;
import ru.yandex.practicum.client.payment.model.PaymentResponse;
import ru.yandex.practicum.market.service.CurrentUserService;

@Service
public class PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);
    private static final String INSUFFICIENT_FUNDS = "InsufficientFundsException";

    private final PaymentApi paymentApi;
    private final CurrentUserService currentUserService;

    public PaymentClient(PaymentApi paymentApi, CurrentUserService currentUserService) {
        this.paymentApi = paymentApi;
        this.currentUserService = currentUserService;
    }

    public Mono<PaymentCheckResult> checkAvailability(long total) {
        if (total < 0) return Mono.error(new IllegalArgumentException("Payment total cannot be negative"));
        if (total == 0) return Mono.just(new PaymentCheckResult(true, null));
        return currentUserService.getCurrentUserId()
            .flatMap(paymentApi::getBalance)
            .doOnError(ex -> log.error("Failed to check payment availability", ex))
            .map(response -> response.getBalance() >= total
                ? new PaymentCheckResult(true, null)
                : new PaymentCheckResult(false, "Недостаточно средств"))
            .onErrorReturn(new PaymentCheckResult(false, "Ошибка. Попробуйте совершить покупку позже."));
    }

    public Mono<PaymentResponse> makePayment(long total) {
        if (total <= 0) {
            return Mono.error(new PaymentClientException("Payment total must be positive"));
        }

        PaymentRequest request = new PaymentRequest().amount((double) total);
        return currentUserService.getCurrentUserId().flatMap(userId ->
            paymentApi.makePaymentWithResponseSpec(userId, request)
                .onStatus(HttpStatusCode::isError, response -> response
                    .bodyToMono(ErrorResponse.class)
                    .defaultIfEmpty(new ErrorResponse().exception("Unknown").message("Unknown error"))
                    .flatMap(error -> {
                        if (INSUFFICIENT_FUNDS.equals(error.getException())) {
                            return Mono.error(new PaymentInsufficientFundsException(error.getMessage()));
                        }
                        return Mono.error(new PaymentClientException(error.getMessage()));
                    })
                )
                .bodyToMono(PaymentResponse.class)
                .onErrorMap(WebClientResponseException.class, ex -> new PaymentClientException(ex.getMessage())));
    }
}
