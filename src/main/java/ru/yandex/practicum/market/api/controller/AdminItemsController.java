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
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.service.AdminItemsService;

@Validated
@Controller
@RequestMapping("/admin/items")
public class AdminItemsController {
    private final AdminItemsService adminItemsService;

    public AdminItemsController(AdminItemsService adminItemsService) {
        this.adminItemsService = adminItemsService;
    }

    @GetMapping("/new")
    public String newItemForm(
        @RequestParam(defaultValue = "false") boolean created,
        Model model
    ) {
        model.addAttribute("created", created);
        return "admin-item-new";
    }

    @PostMapping
    public String createItem(
        @Size(max = 255) @RequestParam String title,
        @Size(max = 4096) @RequestParam String description,
        @Positive @RequestParam long price,
        @RequestParam(required = false) MultipartFile image
    ) {
        adminItemsService.createItem(title, description, price, image);
        return "redirect:/admin/items/new?created=true";
    }
}
