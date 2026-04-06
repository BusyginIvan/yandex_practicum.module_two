package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminItemsServiceTest extends AbstractServiceTest {
    @Autowired
    private AdminItemsService adminItemsService;

    @Test
    void createItem_ShouldSaveItemWithImage() {
        FilePart image = filePart("img.png", "image/png", new byte[]{1, 2, 3});
        when(imageR2dbcRepository.save(any(ImageR2dbcEntity.class)))
            .thenAnswer(invocation -> {
                ImageR2dbcEntity saved = invocation.getArgument(0);
                saved.setId(10L);
                return Mono.just(saved);
            });
        when(itemR2dbcRepository.save(any(ItemR2dbcEntity.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        adminItemsService.createItem("title", "description", 100, image).block();

        ArgumentCaptor<ImageR2dbcEntity> imageCaptor = ArgumentCaptor.forClass(ImageR2dbcEntity.class);
        verify(imageR2dbcRepository).save(imageCaptor.capture());
        ImageR2dbcEntity savedImage = imageCaptor.getValue();
        assertEquals("image/png", savedImage.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, savedImage.getBytes());

        ArgumentCaptor<ItemR2dbcEntity> itemCaptor = ArgumentCaptor.forClass(ItemR2dbcEntity.class);
        verify(itemR2dbcRepository).save(itemCaptor.capture());
        ItemR2dbcEntity savedItem = itemCaptor.getValue();
        assertEquals("title", savedItem.getTitle());
        assertEquals("description", savedItem.getDescription());
        assertEquals(100, savedItem.getPrice());
        assertEquals(10L, savedItem.getImageId());
    }

    @Test
    void createItem_WhenImageContentTypeIsInvalid() {
        FilePart image = filePart("img.txt", "text/plain", new byte[]{1});

        assertThrows(InvalidImageContentTypeException.class, () ->
            adminItemsService.createItem("title", "description", 100, image).block()
        );
    }

    private static FilePart filePart(String filename, String contentType, byte[] bytes) {
        FilePart image = mock(FilePart.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        when(image.headers()).thenReturn(headers);

        when(image.filename()).thenReturn(filename);

        DataBuffer buffer = new DefaultDataBufferFactory().wrap(bytes);
        when(image.content()).thenReturn(Flux.just(buffer));

        return image;
    }
}
