package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.not_found.ImageNotFoundException;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;

@Service
public class ImagesService {
    private final ImageR2dbcRepository imageRepository;

    public ImagesService(ImageR2dbcRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Mono<ImageR2dbcEntity> getImage(long id) {
        return imageRepository.findById(id)
            .switchIfEmpty(Mono.error(new ImageNotFoundException(id)));
    }
}
