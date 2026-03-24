package ru.yandex.practicum.market.integration.payment;

public class PaymentClientException extends RuntimeException {
    public PaymentClientException(String message) {
        super(message);
    }
}
