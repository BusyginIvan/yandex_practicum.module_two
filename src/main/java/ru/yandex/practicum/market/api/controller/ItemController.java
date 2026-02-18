package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;

@Validated
@Controller
@RequestMapping("/items/{id}")
public class ItemController {

    private static final ItemModel ITEM_STUB = new ItemModel(
        1L,
        "title",
        "description",
        100,
        1,
        0
    );

    @GetMapping
    public String getItem(
        @PathVariable Long id,
        Model model
    ) {
        model.addAttribute("item", ITEM_STUB);
        return "item";
    }

    @PostMapping
    public String updateCartItemCounter(
        @Positive @PathVariable Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        model.addAttribute("item", ITEM_STUB);
        return "item";
    }
}
