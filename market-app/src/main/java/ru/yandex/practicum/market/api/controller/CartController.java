package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.integration.payment.PaymentClient;
import ru.yandex.practicum.market.service.CartService;

@Validated
@Controller
@RequestMapping("/cart/items")
public class CartController {
    private final CartService cartService;
    private final PaymentClient paymentClient;

    public CartController(CartService cartService, PaymentClient paymentClient) {
        this.cartService = cartService;
        this.paymentClient = paymentClient;
    }

    @GetMapping
    public Mono<String> getItems(Model model) {
        return cartService.getCart().flatMap(cart ->
            paymentClient.checkAvailability(cart.total()).map(check -> {
                model.addAttribute("items", cart.items());
                model.addAttribute("total", cart.total());
                model.addAttribute("canBuy", check.canBuy());
                model.addAttribute("paymentErrorMessage", check.errorMessage());
                return "cart";
            }));
    }

    @PostMapping
    public Mono<String> changeItemCount(
        @Positive @RequestParam Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        return cartService.changeItemCount(id, action).flatMap(cart ->
            paymentClient.checkAvailability(cart.total()).map(check -> {
                model.addAttribute("items", cart.items());
                model.addAttribute("total", cart.total());
                model.addAttribute("canBuy", check.canBuy());
                model.addAttribute("paymentErrorMessage", check.errorMessage());
                return "cart";
            }));
    }
}
