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
import ru.yandex.practicum.market.api.model.ItemModel;
import ru.yandex.practicum.market.api.model.PagingModel;
import ru.yandex.practicum.market.domain.CartItemCountAction;
import ru.yandex.practicum.market.domain.ItemSort;

import java.util.List;

@Validated
@Controller
@RequestMapping({"/items", "/"})
public class ItemsController {

    private static final PagingModel PAGING_STUB = new PagingModel(
        5,
        1,
        false,
        false
    );
    private static final ItemModel ITEM_STUB = new ItemModel(
        1L,
        "title",
        "description",
        100,
        1,
        0
    );

    @GetMapping
    public String getItems(
        @Size(max = 255) @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "NO") ItemSort sort,
        @Positive @RequestParam(defaultValue = "1") int pageNumber,
        @Positive @RequestParam(defaultValue = "5") int pageSize,
        Model model
    ) {
        model.addAttribute("items", List.of(List.of(ITEM_STUB)));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", PAGING_STUB);

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
        return String.format(
            "redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d",
            search,
            sort,
            pageNumber,
            pageSize
        );
    }
}
