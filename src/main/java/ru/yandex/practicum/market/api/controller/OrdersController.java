package ru.yandex.practicum.market.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.service.OrdersService;

@Validated
@Controller
public class OrdersController {
    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/orders")
    public String getOrders(
        Model model
    ) {
        model.addAttribute("orders", ordersService.getOrders());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean newOrder,
        Model model
    ) {
        model.addAttribute("order", ordersService.getOrder(id));
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy() {
        long orderId = ordersService.buy();
        return String.format("redirect:/orders/%d?newOrder=true", orderId);
    }
}
