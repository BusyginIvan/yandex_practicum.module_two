package ru.yandex.practicum.market.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.market.configuration.PostgresTestConfiguration;
import ru.yandex.practicum.market.exception.validation.EmptyCartException;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@SpringBootTest(properties = "spring.sql.init.mode=always")
public class MarketFlowE2eTest {

    @Autowired private MockMvc mvc;

    @Autowired private DataSource dataSource;

    @BeforeEach
    void beforeEach() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("truncate.sql"));
        populator.execute(dataSource);
    }

    @Test
    void createItem_ShouldAppearInCatalog_ImageShouldBeAvailable() throws Exception {
        createItem("Apple", "Green apple", 100);

        MvcResult itemsResult = mvc.perform(get("/items"))
            .andExpect(status().isOk())
            .andReturn();
        String itemsHtml = itemsResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(itemsHtml.contains("Apple"));
        assertTrue(itemsHtml.contains("Green apple"));

        mvc.perform(get("/images/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"))
            .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void itemsSearchAndSort_ShouldFilterAndSortByPriceAscending() throws Exception {
        createItem("Banana", "Yellow fruit", 300);
        createItem("Apple", "Green fruit", 100);
        createItem("Car", "Vehicle", 50);

        MvcResult result = mvc.perform(get("/items")
                .param("sort", "PRICE")
                .param("search", "")
                .param("pageNumber", "1")
                .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int carIndex = html.indexOf("Car");
        int appleIndex = html.indexOf("Apple");
        int bananaIndex = html.indexOf("Banana");
        if (!(carIndex < appleIndex && appleIndex < bananaIndex)) {
            throw new AssertionError("Expected PRICE sort order: Car, Apple, Banana");
        }

        MvcResult filteredResult = mvc.perform(get("/items")
                .param("search", "vehicle")
                .param("sort", "NO")
                .param("pageNumber", "1")
                .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andReturn();
        String filteredHtml = filteredResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(filteredHtml.contains("Car"));
        assertFalse(filteredHtml.contains("Apple"));
        assertFalse(filteredHtml.contains("Banana"));
    }

    @Test
    void cartAndBuyFlow_ShouldCreateOrderAndClearCart() throws Exception {
        createItem("Apple", "Green apple", 100);
        createItem("Banana", "Rotten banana", 5);
        createItem("Orange", "Fresh orange", 250);

        addItemToCart(1);
        addItemToCart(1);
        addItemToCart(3);

        MvcResult cartResult = mvc.perform(get("/cart/items"))
            .andExpect(status().isOk())
            .andReturn();
        String cartHtml = cartResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(cartHtml.contains("Apple"));
        assertFalse(cartHtml.contains("Banana"));
        assertTrue(cartHtml.contains("Orange"));
        assertTrue(cartHtml.contains("Итого: 450 руб."));

        mvc.perform(post("/buy"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/orders/1?newOrder=true"));

        MvcResult orderResult = mvc.perform(get("/orders/1").param("newOrder", "true"))
            .andExpect(status().isOk())
            .andReturn();
        String orderHtml = orderResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(orderHtml.contains("Поздравляем! Успешная покупка!"));
        assertTrue(orderHtml.contains("Сумма: 450 руб."));

        MvcResult emptyCartResult = mvc.perform(get("/cart/items"))
            .andExpect(status().isOk())
            .andReturn();
        String emptyCartHtml = emptyCartResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertFalse(emptyCartHtml.contains("Apple"));
        assertFalse(emptyCartHtml.contains("Banana"));
        assertFalse(emptyCartHtml.contains("Orange"));
        assertFalse(emptyCartHtml.contains("Купить"));
    }

    @Test
    void buy_WhenCartIsEmpty_ShouldReturnBadRequestErrorPage() throws Exception {
        MvcResult result = mvc.perform(post("/buy"))
            .andExpect(status().isBadRequest())
            .andReturn();
        assertTrue(result.getResolvedException() instanceof EmptyCartException);
    }

    @Test
    void itemPageFlow() throws Exception {
        createItem("Pear", "Sweet pear", 170);

        MvcResult itemPageResult = mvc.perform(get("/items/1"))
            .andExpect(status().isOk())
            .andReturn();
        String itemPageHtml = itemPageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(itemPageHtml.contains("Pear"));
        assertTrue(itemPageHtml.contains("Sweet pear"));
        assertTrue(itemPageHtml.contains(">0<"));

        MvcResult updatedItemPageResult = mvc.perform(post("/items/1").param("action", "PLUS"))
            .andExpect(status().isOk())
            .andReturn();
        String updatedItemPageHtml = updatedItemPageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(updatedItemPageHtml.contains(">1<"));

        MvcResult cartResult = mvc.perform(get("/cart/items"))
            .andExpect(status().isOk())
            .andReturn();
        String cartHtml = cartResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(cartHtml.contains("Pear"));
        assertTrue(cartHtml.contains("Итого: 170 руб."));
    }

    private void createItem(String title, String description, long price) throws Exception {
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            "image/png",
            new byte[]{1, 2, 3}
        );

        mvc.perform(multipart("/admin/items")
                .file(image)
                .param("title", title)
                .param("description", description)
                .param("price", String.valueOf(price)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/items/new?created=true"));
    }

    private void addItemToCart(long itemId) throws Exception {
        mvc.perform(post("/items")
                .param("id", String.valueOf(itemId))
                .param("action", "PLUS"))
            .andExpect(status().is3xxRedirection());
    }
}
