package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.not_found.ImageNotFoundException;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ImagesServiceTest extends AbstractServiceTest {
    @Autowired
    private ImagesService imagesService;

    @Test
    void getImage_ShouldReturnImage() {
        ImageR2dbcEntity image = new ImageR2dbcEntity();
        image.setId(1L);
        image.setContentType("image/png");
        image.setBytes(new byte[0]);
        when(imageR2dbcRepository.findById(1L)).thenReturn(Mono.just(image));

        ImageR2dbcEntity actual = imagesService.getImage(1L).block();

        assertSame(image, actual);
    }

    @Test
    void getImage_WhenNotFound_ShouldThrowImageNotFoundException() {
        when(imageR2dbcRepository.findById(1L)).thenReturn(Mono.empty());

        ImageNotFoundException exception = assertThrows(
            ImageNotFoundException.class,
            () -> imagesService.getImage(1L).block()
        );

        assertEquals("Image not found: 1", exception.getMessage());
    }
}
