package ru.yandex.practicum.market.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.security.UserDetailsImpl;

@Service
public class CurrentUserService {
    public Mono<Long> getCurrentUserId() {
        return getCurrentUserIdOrEmpty()
            .switchIfEmpty(Mono.error(new AccessDeniedException("Authentication is required")));
    }

    public Mono<Long> getCurrentUserIdOrEmpty() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(this::isAuthenticated)
            .map(this::getUserId);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) return userDetails.getUserId();
        throw new IllegalStateException(
            "Authenticated principal must be UserDetailsImpl, but was " + principal.getClass().getName()
        );
    }
}
