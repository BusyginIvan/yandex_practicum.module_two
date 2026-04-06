package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment.model.PaymentRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {

    @Autowired private WebTestClient webTestClient;
    @LocalServerPort private int port;

    @Test
    void getBalance_ShouldReturnFixedBalance() {
        webTestClient.get()
            .uri("http://localhost:" + port + "/balance")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.balance").isEqualTo(1000.0);
    }

    @Test
    void makePayment_ShouldReturnNewBalance() {
        PaymentRequest request = new PaymentRequest().amount(100.0);

        webTestClient.post()
            .uri("http://localhost:" + port + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.balance").isEqualTo(900.0);
    }

    @Test
    void makePayment_WhenInsufficientFunds_ShouldReturnError() {
        PaymentRequest request = new PaymentRequest().amount(2000.0);

        webTestClient.post()
            .uri("http://localhost:" + port + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.exception").isEqualTo("InsufficientFundsException")
            .jsonPath("$.message").isEqualTo("Insufficient funds");
    }
}
