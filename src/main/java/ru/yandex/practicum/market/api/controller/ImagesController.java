package ru.yandex.practicum.market.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.market.persistence.entity.ImageEntity;
import ru.yandex.practicum.market.service.ImagesService;

@Validated
@RestController
@RequestMapping("/images/{id}")
public class ImagesController {
    private final ImagesService imagesService;

    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    @GetMapping
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        ImageEntity image = imagesService.getImage(id);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .body(image.getBytes());
    }
}
