package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.not_found.ImageNotFoundException;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;
import ru.yandex.practicum.market.redis.ImagesCacheService;

@Service
public class ImagesService {
    private final ImagesCacheService imagesCacheService;

    public ImagesService(ImagesCacheService imagesCacheService) {
        this.imagesCacheService = imagesCacheService;
    }

    public Mono<ImageR2dbcEntity> getImage(long id) {
        return imagesCacheService.getById(id)
            .switchIfEmpty(Mono.error(new ImageNotFoundException(id)));
    }
}
