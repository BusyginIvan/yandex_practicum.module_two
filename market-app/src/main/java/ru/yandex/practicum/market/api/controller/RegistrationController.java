package ru.yandex.practicum.market.api.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.api.model.RegistrationForm;
import ru.yandex.practicum.market.service.UserRegistrationService;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    private final UserRegistrationService userRegistrationService;

    public RegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping
    public String getRegistrationPage(@ModelAttribute("form") RegistrationForm form, Model model) {
        model.addAttribute("registered", false);
        return "register";
    }

    @PostMapping
    public Mono<String> register(
        @Valid @ModelAttribute("form") RegistrationForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registered", false);
            return Mono.just("register");
        }

        return userRegistrationService.register(form.getUsername(), form.getPassword())
            .thenReturn("redirect:/login")
            .onErrorResume(IllegalArgumentException.class, ex -> {
                model.addAttribute("registrationError", ex.getMessage());
                model.addAttribute("registered", false);
                return Mono.just("register");
            });
    }
}
