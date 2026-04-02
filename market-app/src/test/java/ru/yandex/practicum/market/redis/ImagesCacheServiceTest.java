package ru.yandex.practicum.market.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.persistence.entity.ImageR2dbcEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImagesCacheServiceTest extends AbstractCacheServiceTest {
    @Autowired
    private ImagesCacheService imagesCacheService;

    @Test
    void getById_WhenCached_ShouldReturnImage() {
        byte[] contentTypeBytes = "image/png".getBytes(StandardCharsets.UTF_8);
        byte[] imageBytes = new byte[]{1, 2, 3};
        ReactiveHashOperations<String, String, byte[]> hashOps = mock(ReactiveHashOperations.class);
        doReturn(hashOps).when(imageHashRedisTemplate).opsForHash();
        when(hashOps.multiGet(eq("image:1"), any()))
            .thenReturn(Mono.just(List.of(contentTypeBytes, imageBytes)));

        ImageR2dbcEntity actual = imagesCacheService.getById(1L).block();

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        assertEquals("image/png", actual.getContentType());
        assertEquals(3, actual.getBytes().length);
    }

    @Test
    void getById_WhenMissing_ShouldLoadFromDbAndCache() {
        ReactiveHashOperations<String, String, byte[]> hashOps = mock(ReactiveHashOperations.class);
        doReturn(hashOps).when(imageHashRedisTemplate).opsForHash();
        when(hashOps.multiGet(eq("image:1"), any())).thenReturn(Mono.just(List.of()));

        ImageR2dbcEntity image = new ImageR2dbcEntity();
        image.setId(1L);
        image.setContentType("image/png");
        image.setBytes(new byte[]{9, 9});
        when(imageR2dbcRepository.findById(1L)).thenReturn(Mono.just(image));
        when(hashOps.putAll(eq("image:1"), any(Map.class))).thenReturn(Mono.just(true));
        when(imageHashRedisTemplate.expire(eq("image:1"), any())).thenReturn(Mono.just(true));

        ImageR2dbcEntity actual = imagesCacheService.getById(1L).block();

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        verify(imageR2dbcRepository).findById(1L);
        verify(hashOps).putAll(eq("image:1"), any(Map.class));
        verify(imageHashRedisTemplate).expire(eq("image:1"), any());
    }
}
