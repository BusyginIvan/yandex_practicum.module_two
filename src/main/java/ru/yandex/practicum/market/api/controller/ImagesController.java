package ru.yandex.practicum.market.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/images/{id}")
public class ImagesController {
    @GetMapping
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] bytes = new byte[] {1, 2, 3};
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(bytes);
    }
}
