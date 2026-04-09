package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.PaymentRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {
    private static final String TEST_TOKEN = "test-token";

    @Autowired private WebTestClient webTestClient;
    @LocalServerPort private int port;
    @MockitoBean private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Jwt jwt = Jwt.withTokenValue(TEST_TOKEN)
            .header("alg", "none")
            .claim("sub", "market-client")
            .claim("client_id", "market-client")
            .build();

        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
    }

    @Test
    void getBalance_ShouldReturnFixedBalance() {
        webTestClient.get()
            .uri("http://localhost:" + port + "/balance")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
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
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
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
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.exception").isEqualTo("InsufficientFundsException")
            .jsonPath("$.message").isEqualTo("Insufficient funds");
    }

    @Test
    void getBalance_WithoutToken_ShouldReturnUnauthorized() {
        webTestClient.get()
            .uri("http://localhost:" + port + "/balance")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void makePayment_WithoutToken_ShouldReturnUnauthorized() {
        PaymentRequest request = new PaymentRequest().amount(100.0);

        webTestClient.post()
            .uri("http://localhost:" + port + "/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
