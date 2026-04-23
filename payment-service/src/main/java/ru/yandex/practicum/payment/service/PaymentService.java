package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    private static final double DEFAULT_BALANCE = 1000.0;

    private final Map<Long, Double> balances = new ConcurrentHashMap<>();

    public double getBalance(Long userId) {
        return balances.computeIfAbsent(userId, ignored -> DEFAULT_BALANCE);
    }

    public double makePayment(Long userId, double amount) {
        double balance = getBalance(userId);

        if (amount > balance) throw new InsufficientFundsException();

        double newBalance = balance - amount;
        balances.put(userId, newBalance);
        return newBalance;
    }
}
