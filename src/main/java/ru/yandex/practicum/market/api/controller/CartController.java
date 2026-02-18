package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;

import java.util.List;

@Validated
@Controller
@RequestMapping("/cart/items")
public class CartController {

    private static final ItemModel ITEM_STUB = new ItemModel(
        1L,
        "title",
        "description",
        100,
        1,
        0
    );

    @GetMapping
    public String getItems(Model model) {
        model.addAttribute("items", List.of(ITEM_STUB));
        model.addAttribute("total", 100);

        return "cart";
    }

    @PostMapping
    public String changeItemCount(
        @Positive @RequestParam Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        model.addAttribute("items", List.of(ITEM_STUB));
        model.addAttribute("total", 100);

        return "cart";
    }
}
