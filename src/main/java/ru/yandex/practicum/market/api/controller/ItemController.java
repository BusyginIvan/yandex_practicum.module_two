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
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.service.ItemService;

@Validated
@Controller
@RequestMapping("/items/{id}")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public String getItem(
        @Positive @PathVariable Long id,
        Model model
    ) {
        model.addAttribute("item", itemService.getItem(id));
        return "item";
    }

    @PostMapping
    public String updateCartItemCounter(
        @Positive @PathVariable Long id,
        @RequestParam CartItemCountAction action,
        Model model
    ) {
        model.addAttribute("item", itemService.updateCartItemCount(id, action));
        return "item";
    }
}
