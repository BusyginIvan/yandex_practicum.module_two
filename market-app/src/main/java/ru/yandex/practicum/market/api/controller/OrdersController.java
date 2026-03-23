package ru.yandex.practicum.market.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.service.OrdersService;

@Validated
@Controller
public class OrdersController {
    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return ordersService.getOrders().map(orders -> {
            model.addAttribute("orders", orders);
            return "orders";
        });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean newOrder,
        Model model
    ) {
        return ordersService.getOrder(id).map(order -> {
            model.addAttribute("order", order);
            model.addAttribute("newOrder", newOrder);
            return "order";
        });
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return ordersService.buy()
            .map(orderId -> String.format("redirect:/orders/%d?newOrder=true", orderId));
    }
}
