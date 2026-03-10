package ru.yandex.practicum.market.service;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.exception.validation.ImageRequiredException;
import ru.yandex.practicum.market.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;
import ru.yandex.practicum.market.persistence.entity.ItemR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;
import ru.yandex.practicum.market.persistence.repository.ItemR2dbcRepository;

@Service
public class AdminItemsService {
    private final ItemR2dbcRepository itemRepository;
    private final ImageR2dbcRepository imageRepository;

    public AdminItemsService(
        ItemR2dbcRepository itemRepository,
        ImageR2dbcRepository imageRepository
    ) {
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
    }

    public Mono<ItemR2dbcEntity> createItem(
        String title,
        String description,
        long price,
        FilePart image
    ) {
        return saveImage(image).flatMap(savedImage -> {
            ItemR2dbcEntity item = new ItemR2dbcEntity();
            item.setTitle(title);
            item.setDescription(description);
            item.setPrice(price);
            item.setImageId(savedImage.getId());
            return itemRepository.save(item);
        });
    }

    private Mono<ImageR2dbcEntity> saveImage(FilePart image) {
        MediaType contentType = image.headers().getContentType();
        if (contentType == null || !"image".equalsIgnoreCase(contentType.getType())) {
            return Mono.error(new InvalidImageContentTypeException(contentType));
        }

        return DataBufferUtils.join(image.content()).flatMap(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            if (bytes.length == 0) throw new ImageRequiredException();

            ImageR2dbcEntity entity = new ImageR2dbcEntity();
            entity.setContentType(contentType.toString());
            entity.setBytes(bytes);
            return imageRepository.save(entity);
        });
    }
}
