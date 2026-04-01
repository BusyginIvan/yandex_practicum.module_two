package ru.yandex.practicum.market.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.netty.http.client.HttpClient;
import ru.yandex.practicum.market.configuration.PostgresTestConfiguration;
import ru.yandex.practicum.market.configuration.RedisTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
    PostgresTestConfiguration.class,
    RedisTestConfiguration.class
})
@SpringBootTest(
    properties = "spring.sql.init.mode=always",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MarketFlowE2eTest {

    private WebTestClient webTestClient;

    @LocalServerPort private int port;

    @Autowired private DatabaseClient databaseClient;
    @Autowired private ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void beforeEach() {
        List<String> statements = List.of(
            "TRUNCATE TABLE order_item_counts RESTART IDENTITY CASCADE",
            "TRUNCATE TABLE cart_item_counts RESTART IDENTITY CASCADE",
            "TRUNCATE TABLE orders RESTART IDENTITY CASCADE",
            "TRUNCATE TABLE items RESTART IDENTITY CASCADE",
            "TRUNCATE TABLE images RESTART IDENTITY CASCADE"
        );
        statements.forEach(sql -> databaseClient.sql(sql).fetch().rowsUpdated().block());

        redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().block();

        HttpClient httpClient = HttpClient.create().followRedirect(false);
        webTestClient = WebTestClient.bindToServer()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @Test
    void createItem_ShouldAppearInCatalog_ImageShouldBeAvailable() throws Exception {
        createItem("Apple", "Green apple", 100);

        webTestClient.get().uri("/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Apple"));
                assertTrue(html.contains("Green apple"));
            });

        webTestClient.get().uri("/images/1")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.IMAGE_PNG)
            .expectBody(byte[].class)
            .isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void itemsSearchAndSort_ShouldFilterAndSortByPriceAscending() throws Exception {
        createItem("Banana", "Yellow fruit", 300);
        createItem("Apple", "Green fruit", 100);
        createItem("Car", "Vehicle", 50);

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/items")
                .queryParam("sort", "PRICE")
                .queryParam("search", "")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "5")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                int carIndex = html.indexOf("Car");
                int appleIndex = html.indexOf("Apple");
                int bananaIndex = html.indexOf("Banana");
                if (!(carIndex < appleIndex && appleIndex < bananaIndex)) {
                    throw new AssertionError("Expected PRICE sort order: Car, Apple, Banana");
                }
            });

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/items")
                .queryParam("search", "vehicle")
                .queryParam("sort", "NO")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "5")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Car"));
                assertFalse(html.contains("Apple"));
                assertFalse(html.contains("Banana"));
            });
    }

    @Test
    void cartAndBuyFlow_ShouldCreateOrderAndClearCart() throws Exception {
        createItem("Apple", "Green apple", 100);
        createItem("Banana", "Rotten banana", 5);
        createItem("Orange", "Fresh orange", 250);

        addItemToCart(1);
        addItemToCart(1);
        addItemToCart(3);

        webTestClient.get().uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Apple"));
                assertFalse(html.contains("Banana"));
                assertTrue(html.contains("Orange"));
                assertTrue(html.contains("Итого: 450 руб."));
            });

        webTestClient.post().uri("/buy")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path("/orders/1")
                .queryParam("newOrder", "true")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Поздравляем! Успешная покупка!"));
                assertTrue(html.contains("Сумма: 450 руб."));
            });

        webTestClient.get().uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertFalse(html.contains("Apple"));
                assertFalse(html.contains("Banana"));
                assertFalse(html.contains("Orange"));
                assertFalse(html.contains("Купить"));
            });
    }

    @Test
    void buy_WhenCartIsEmpty_ShouldReturnBadRequestErrorPage() throws Exception {
        webTestClient.post().uri("/buy")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void itemPageFlow() throws Exception {
        createItem("Pear", "Sweet pear", 170);

        webTestClient.get().uri("/items/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Pear"));
                assertTrue(html.contains("Sweet pear"));
                assertTrue(html.contains(">0<"));
            });

        webTestClient.post()
            .uri(uriBuilder -> uriBuilder.path("/items/1").queryParam("action", "PLUS").build())
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains(">1<"));
            });

        webTestClient.get().uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String html = result.getResponseBody();
                assertNotNull(html);
                assertTrue(html.contains("Pear"));
                assertTrue(html.contains("Итого: 170 руб."));
            });
    }

    private void createItem(String title, String description, long price) throws Exception {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new ByteArrayResource(new byte[]{1, 2, 3}) {
                @Override
                public String getFilename() {
                    return "image.png";
                }
            })
            .contentType(MediaType.IMAGE_PNG);
        builder.part("title", title).contentType(MediaType.TEXT_PLAIN);
        builder.part("description", description).contentType(MediaType.TEXT_PLAIN);
        builder.part("price", String.valueOf(price)).contentType(MediaType.TEXT_PLAIN);

        webTestClient.post().uri("/admin/items")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/admin/items/new?created=true");
    }

    private void addItemToCart(long itemId) throws Exception {
        webTestClient.post()
            .uri(uriBuilder -> uriBuilder.path("/items")
                .queryParam("id", String.valueOf(itemId))
                .queryParam("action", "PLUS")
                .build())
            .exchange()
            .expectStatus().is3xxRedirection();
    }
}
