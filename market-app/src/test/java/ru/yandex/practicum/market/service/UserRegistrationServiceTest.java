package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.UserR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.UserR2dbcRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserRegistrationServiceTest extends AbstractServiceTest {
    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserR2dbcRepository userR2dbcRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowIllegalArgumentException() {
        when(userR2dbcRepository.existsByUsername("alice")).thenReturn(Mono.just(true));

        IllegalArgumentException actual = assertThrows(
            IllegalArgumentException.class,
            () -> userRegistrationService.register("alice", "password").block()
        );

        assertEquals("User with this username already exists", actual.getMessage());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_ShouldEncodePasswordAndSaveUser() {
        when(userR2dbcRepository.existsByUsername("alice")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userR2dbcRepository.save(any(UserR2dbcEntity.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        userRegistrationService.register("alice", "password").block();

        ArgumentCaptor<UserR2dbcEntity> captor = ArgumentCaptor.forClass(UserR2dbcEntity.class);
        verify(userR2dbcRepository).save(captor.capture());
        UserR2dbcEntity savedUser = captor.getValue();
        assertEquals("alice", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
    }
}
