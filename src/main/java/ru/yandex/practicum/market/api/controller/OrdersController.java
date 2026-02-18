package ru.yandex.practicum.market.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.api.model.OrderItemModel;
import ru.yandex.practicum.market.api.model.OrderModel;

import java.util.List;

@Validated
@Controller
public class OrdersController {
    private static final OrderItemModel ITEM_STUB = new OrderItemModel(
        1L,
        "title",
        100,
        0
    );
    private static final OrderModel ORDER_STUB = new OrderModel(
        1L,
        List.of(ITEM_STUB),
        100
    );

    @GetMapping("/orders")
    public String getOrders(
        Model model
    ) {
        model.addAttribute("orders", List.of(ORDER_STUB));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean newOrder,
        Model model
    ) {
        model.addAttribute("order", ORDER_STUB);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy() {
        return String.format("redirect:/orders/%d?newOrder=true", 0);
    }
}
