package ru.yandex.practicum.market.redis;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;
import ru.yandex.practicum.market.persistence.repository.ImageR2dbcRepository;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ImagesCacheService {
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String FIELD_CONTENT_TYPE = "contentType";
    private static final String FIELD_BYTES = "bytes";

    private final ReactiveRedisTemplate<String, byte[]> imageHashRedisTemplate;
    private final ImageR2dbcRepository imageRepository;

    public ImagesCacheService(
        ReactiveRedisTemplate<String, byte[]> imageHashRedisTemplate,
        ImageR2dbcRepository imageRepository
    ) {
        this.imageHashRedisTemplate = imageHashRedisTemplate;
        this.imageRepository = imageRepository;
    }

    public Mono<ImageR2dbcEntity> getById(long id) {
        String key = "image:" + id;
        ReactiveHashOperations<String, String, byte[]> hashOps = imageHashRedisTemplate.opsForHash();
        return hashOps.multiGet(key, List.of(FIELD_CONTENT_TYPE, FIELD_BYTES))
            .flatMap(values -> {
                byte[] contentTypeBytes = values.size() > 0 ? values.get(0) : null;
                byte[] imageBytes = values.size() > 1 ? values.get(1) : null;
                if (contentTypeBytes == null || imageBytes == null) return Mono.empty();
                ImageR2dbcEntity cached = new ImageR2dbcEntity();
                cached.setId(id);
                cached.setContentType(new String(contentTypeBytes, UTF_8));
                cached.setBytes(imageBytes);
                return Mono.just(cached);
            })
            .switchIfEmpty(Mono.defer(() ->
                imageRepository.findById(id).flatMap(image -> {
                    Map<String, byte[]> fields = new HashMap<>();
                    fields.put(FIELD_CONTENT_TYPE, image.getContentType().getBytes(UTF_8));
                    fields.put(FIELD_BYTES, image.getBytes());
                    return hashOps.putAll(key, fields)
                        .then(imageHashRedisTemplate.expire(key, TTL))
                        .thenReturn(image);
                })
            ));
    }
}
