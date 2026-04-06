package ru.yandex.practicum.market.integration.payment;

public record PaymentCheckResult(boolean canBuy, String errorMessage) { }
