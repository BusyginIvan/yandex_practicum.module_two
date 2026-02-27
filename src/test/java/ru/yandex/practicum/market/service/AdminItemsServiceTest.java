package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.exception.validation.ImageRequiredException;
import ru.yandex.practicum.market.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminItemsServiceTest extends AbstractServiceTest {
    @Autowired
    private AdminItemsService adminItemsService;

    @Test
    void createItem_ShouldSaveItemWithImage() {
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "img.png",
            "image/png",
            new byte[]{1, 2, 3}
        );

        adminItemsService.createItem("title", "description", 100, image);

        ArgumentCaptor<ItemEntity> captor = ArgumentCaptor.forClass(ItemEntity.class);
        verify(itemRepository).save(captor.capture());
        ItemEntity saved = captor.getValue();
        assertEquals("title", saved.getTitle());
        assertEquals("description", saved.getDescription());
        assertEquals(100, saved.getPrice());
        assertEquals("image/png", saved.getImage().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, saved.getImage().getBytes());
    }

    @Test
    void createItem_WhenImageMissing() {
        assertThrows(ImageRequiredException.class, () ->
            adminItemsService.createItem("title", "description", 100, null)
        );
    }

    @Test
    void createItem_WhenImageContentTypeIsInvalid() {
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "img.txt",
            "text/plain",
            new byte[]{1}
        );

        assertThrows(InvalidImageContentTypeException.class, () ->
            adminItemsService.createItem("title", "description", 100, image)
        );
    }

    @Test
    void createItem_WhenImageReadFails() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/png");
        when(image.getBytes()).thenThrow(new IOException("boom"));

        assertThrows(UncheckedIOException.class, () ->
            adminItemsService.createItem("title", "description", 100, image)
        );
    }
}
