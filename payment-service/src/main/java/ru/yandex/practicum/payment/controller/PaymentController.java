package ru.yandex.practicum.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.BalanceApi;
import ru.yandex.practicum.payment.api.PaymentApi;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.ErrorResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;

@RestController
public class PaymentController implements BalanceApi, PaymentApi {
    private static final double BALANCE = 1000.0;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        BalanceResponse response = new BalanceResponse().balance(BALANCE);
        return Mono.just(ResponseEntity.ok(response));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(
        Mono<PaymentRequest> paymentRequest,
        ServerWebExchange exchange
    ) {
        return paymentRequest.flatMap(request -> {
            Double amount = request.getAmount();
            if (amount > BALANCE) return Mono.error(new InsufficientFundsException());
            double newBalance = BALANCE - amount;
            PaymentResponse response = new PaymentResponse().balance(newBalance);
            return Mono.just(ResponseEntity.ok(response));
        });
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientFunds() {
        ErrorResponse error = new ErrorResponse()
            .exception(InsufficientFundsException.class.getSimpleName())
            .message("Insufficient funds");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationError(WebExchangeBindException ex) {
        ErrorResponse error = new ErrorResponse()
            .exception(ex.getClass().getSimpleName())
            .message("Invalid request");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAnyException(Exception ex) {
        ErrorResponse error = new ErrorResponse()
            .exception(ex.getClass().getSimpleName())
            .message("Unexpected error");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    private static class InsufficientFundsException extends RuntimeException { }
}
