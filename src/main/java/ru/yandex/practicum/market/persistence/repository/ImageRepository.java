package ru.yandex.practicum.market.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.persistence.entity.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> { }
