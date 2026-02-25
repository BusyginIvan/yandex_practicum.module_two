package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.market.exception.not_found.ImageNotFoundException;
import ru.yandex.practicum.market.persistence.entity.ImageEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ImagesServiceTest extends AbstractServiceTest {
    @Autowired
    private ImagesService imagesService;

    @Test
    void getImage_ShouldReturnImage() {
        ImageEntity image = new ImageEntity();
        image.setId(1L);
        image.setContentType("image/png");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        ImageEntity actual = imagesService.getImage(1L);

        assertSame(image, actual);
    }

    @Test
    void getImage_WhenNotFound_ShouldThrowImageNotFoundException() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        ImageNotFoundException exception = assertThrows(
            ImageNotFoundException.class,
            () -> imagesService.getImage(1L)
        );

        assertEquals("Image not found: 1", exception.getMessage());
    }
}
