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
import ru.yandex.practicum.market.service.CartService;

@Validated
@Controller
@RequestMapping("/cart/items")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> getItems(Model model) {
        return cartService.getCart().map(cart -> {
            model.addAttribute("items", cart.items());
            model.addAttribute("total", cart.total());
            return "cart";
        });
    }

    @PostMapping
    public Mono<String> changeItemCount(
        @Positive @RequestParam Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        return cartService.changeItemCount(id, action).map(cart -> {
            model.addAttribute("items", cart.items());
            model.addAttribute("total", cart.total());
            return "cart";
        });
    }
}
