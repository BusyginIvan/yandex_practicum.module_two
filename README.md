# Витрина интернет-магазина + сервис платежей (Spring Boot, WebFlux)

Учебный мультипроект. Основное приложение — витрина товаров с корзиной и заказами. Отдельный сервис платежей отвечает за проверку баланса и проведение оплаты. Данные товаров кешируются в Redis.

## Модули

- `market-app` — витрина интернет-магазина.
- `payment-service` — сервис платежей.
- `openapi/payment-api.yaml` — OpenAPI спецификация обмена между витриной и платежным сервисом.

## Технологии

- Java 21
- Spring Boot 3.4
- Spring WebFlux
- Thymeleaf
- Spring Data R2DBC (PostgreSQL)
- Spring Data Redis Reactive
- OpenAPI Generator
- Maven
- JUnit 5, Spring Boot Test, WebTestClient, Testcontainers
- Docker / Docker Compose

## Основная функциональность витрины

- Просмотр товаров, поиск, сортировка (`NO`, `ALPHA`, `PRICE`), пагинация (`2, 5, 10, 20, 50, 100`).
- Корзина: добавление/изменение количества, удаление, подсчет суммы.
- Заказы: создание заказа и просмотр истории.
- Админ-форма добавления товара: `/admin/items/new`.
- Изображения товаров доступны по `GET /images/{id}`.

## Сервис платежей

REST эндпоинты:
- `GET /balance` — возвращает текущий баланс (всегда `1000`).
- `POST /payment` — попытка списания суммы.

Витрина при открытии корзины запрашивает баланс и блокирует кнопку покупки при недостатке средств. При покупке перед созданием заказа выполняется платеж.

## Redis кеш

Кешируются:
- товары по ключу `item:{id}`;
- страницы списка товаров `items:page={page}:size={size}[:search=...][:sort=...]`;
- изображения товаров `image:{id}` как Redis Hash с полями `contentType` и `bytes`.

## Переменные окружения

Витрина (`market-app`) использует:
- `R2DBC_URL` (по умолчанию `r2dbc:postgresql://localhost:5432/market`)
- `DB_USERNAME` (по умолчанию `market_user`)
- `DB_PASSWORD` (по умолчанию `market_password`)
- `REDIS_HOST` (по умолчанию `localhost`)
- `REDIS_PORT` (по умолчанию `6379`)
- `PAYMENT_BASE_URL` (по умолчанию `http://localhost:8081`)

Для Docker Compose используются значения из `.env` (см. `.env.example`).

## Запуск локально

1. Поднимите PostgreSQL и Redis.
2. Запустите платежный сервис:

```bash
./mvnw -pl payment-service spring-boot:run
```

3. Запустите витрину:

```bash
./mvnw -pl market-app spring-boot:run
```

Витрина будет доступна на `http://localhost:8080`, сервис платежей — на `http://localhost:8081`.

## Запуск в Docker

```bash
docker compose up --build
```

Поднимутся PostgreSQL, Redis, `market-app` и `payment-service`.

Остановка:

```bash
docker compose down
```

## Тесты

Запуск всех тестов:

```bash
./mvnw test
```

Тесты `market-app` используют Testcontainers для PostgreSQL и Redis.

## Сборка

```bash
./mvnw -DskipTests package
```

Собираются оба модуля.
