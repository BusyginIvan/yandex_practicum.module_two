package ru.yandex.practicum.market.integration.payment;

public class PaymentInsufficientFundsException extends RuntimeException {
    public PaymentInsufficientFundsException(String message) {
        super(message);
    }
}
