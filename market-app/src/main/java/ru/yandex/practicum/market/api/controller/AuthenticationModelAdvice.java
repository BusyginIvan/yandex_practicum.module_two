package ru.yandex.practicum.market.api.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;

import java.security.Principal;

@ControllerAdvice
public class AuthenticationModelAdvice {

    @ModelAttribute("authenticated")
    public Mono<Boolean> authenticated(Mono<Principal> principal) {
        return principal
            .map(this::isAuthenticated)
            .defaultIfEmpty(false);
    }

    private boolean isAuthenticated(Principal principal) {
        return principal instanceof Authentication authentication
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
