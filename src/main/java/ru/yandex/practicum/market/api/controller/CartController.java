package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.api.model.CartModel;
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
    public String getItems(Model model) {
        CartModel cart = cartService.getCart();
        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.total());

        return "cart";
    }

    @PostMapping
    public String changeItemCount(
        @Positive @RequestParam Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        CartModel cart = cartService.changeItemCount(id, action);
        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.total());

        return "cart";
    }
}
