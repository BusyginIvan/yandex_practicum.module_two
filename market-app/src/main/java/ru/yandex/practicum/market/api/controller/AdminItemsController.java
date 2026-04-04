package ru.yandex.practicum.market.api.controller;

import jakarta.validation.constraints.Size;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.validation.ValidationException;
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
    public Mono<String> createItem(
        @Size(max = 255) @RequestPart String title,
        @Size(max = 4096) @RequestPart String description,
        @RequestPart String price,
        @RequestPart FilePart image
    ) {
        return adminItemsService.createItem(title, description, parsePrice(price), image)
            .thenReturn("redirect:/admin/items/new?created=true");
    }

    @ExceptionHandler({
        ValidationException.class,
        HandlerMethodValidationException.class,
        ServerWebInputException.class
    })
    public Mono<String> handleValidationException(Exception e, Model model) {
        model.addAttribute("errorStatus", 400);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("created", false);
        return Mono.just("admin-item-new");
    }

    private long parsePrice(String rawPrice) {
        try {
            long value = Long.parseLong(rawPrice);
            if (value <= 0) throw new ValidationException("Price must be positive");
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("Price must be a positive number", e);
        }
    }
}
