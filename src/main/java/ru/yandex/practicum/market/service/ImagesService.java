package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.market.exception.not_found.ImageNotFoundException;
import ru.yandex.practicum.market.persistence.entity.ImageEntity;
import ru.yandex.practicum.market.persistence.repository.ImageRepository;

@Service
public class ImagesService {
    private final ImageRepository imageRepository;

    public ImagesService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true)
    public ImageEntity getImage(long id) {
        return imageRepository.findById(id).orElseThrow(() -> new ImageNotFoundException(id));
    }
}
