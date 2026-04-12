package ru.yandex.practicum.market.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.repository.UserR2dbcRepository;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
    private final UserR2dbcRepository userRepository;

    public UserDetailsServiceImpl(UserR2dbcRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userEntity ->
            new UserDetailsImpl(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                List.of(new SimpleGrantedAuthority("USER"))
            )
        );
    }
}
