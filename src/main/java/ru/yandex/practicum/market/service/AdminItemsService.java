package ru.yandex.practicum.market.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.exception.validation.ImageRequiredException;
import ru.yandex.practicum.market.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.market.persistence.entity.ImageEntity;
import ru.yandex.practicum.market.persistence.entity.ItemEntity;
import ru.yandex.practicum.market.persistence.repository.ItemRepository;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class AdminItemsService {
    private final ItemRepository itemRepository;

    public AdminItemsService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void createItem(
        String title,
        String description,
        long price,
        MultipartFile image
    ) {
        ItemEntity item = new ItemEntity();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setImage(newImageEntity(image));
        itemRepository.save(item);
    }

    private static ImageEntity newImageEntity(MultipartFile image) {
        if (image == null || image.isEmpty()) throw new ImageRequiredException();
        ImageEntity entity = new ImageEntity();
        entity.setContentType(getImageContentType(image));
        entity.setBytes(getImageBytes(image));
        return entity;
    }

    private static String getImageContentType(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageContentTypeException(contentType);
        }
        return contentType;
    }

    private static byte[] getImageBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
