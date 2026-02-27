package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.market.api.model.ItemsPageModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.domain.ItemSort;
import ru.yandex.practicum.market.service.ItemService;
import ru.yandex.practicum.market.service.ItemsService;

@Validated
@Controller
@RequestMapping({"/items", "/"})
public class ItemsController {
    private final ItemsService itemsService;
    private final ItemService itemService;

    public ItemsController(ItemsService itemsService, ItemService itemService) {
        this.itemsService = itemsService;
        this.itemService = itemService;
    }

    @GetMapping
    public String getItems(
        @Size(max = 255) @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "NO") ItemSort sort,
        @Positive @RequestParam(defaultValue = "1") int pageNumber,
        @Positive @RequestParam(defaultValue = "5") int pageSize,
        Model model
    ) {
        ItemsPageModel itemsPage = itemsService.getItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", itemsPage.items());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", itemsPage.paging());

        return "items";
    }

    @PostMapping
    public String updateCartItemCounter(
        @Positive @RequestParam Long id,
        @RequestParam CartItemCountAction action,
        @Size(max = 255) @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "NO") ItemSort sort,
        @Positive @RequestParam(defaultValue = "1") int pageNumber,
        @Positive @RequestParam(defaultValue = "5") int pageSize
    ) {
        itemService.updateCartItemCount(id, action);

        return String.format(
            "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d",
            search,
            sort,
            pageNumber,
            pageSize
        );
    }
}
