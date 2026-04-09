package ru.yandex.practicum.market.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.UserR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.UserR2dbcRepository;

@Service
public class UserRegistrationService {
    private final UserR2dbcRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserR2dbcRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<Void> register(String username, String password) {
        return userRepository.existsByUsername(username)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new IllegalArgumentException("User with this username already exists"));
                }

                UserR2dbcEntity user = new UserR2dbcEntity();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));

                return userRepository.save(user).then();
            });
    }
}
