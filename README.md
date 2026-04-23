# Витрина интернет-магазина и сервис платежей

Учебный многомодульный проект на Spring Boot WebFlux. Основное приложение `market-app` реализует витрину товаров, корзину, заказы, регистрацию и вход пользователей. Отдельный модуль `payment-service` отвечает за проверку баланса и проведение платежей.

## Модули

- `market-app` — витрина интернет-магазина.
- `payment-service` — сервис платежей.
- `openapi/payment-api.yaml` — OpenAPI спецификация обмена между витриной и платежным сервисом.

## Технологии

- Java 21
- Spring Boot 3.4
- Spring WebFlux
- Spring Security: form login, OAuth2 client credentials
- Thymeleaf
- Spring Data R2DBC
- PostgreSQL
- Reactive Redis
- OpenAPI Generator
- Maven
- JUnit 5, Mockito, WebTestClient, Testcontainers
- Docker Compose

## Возможности `market-app`

- Просмотр товаров, поиск, сортировка (`NO`, `ALPHA`, `PRICE`), пагинация (`2, 5, 10, 20, 50, 100`).
- Корзина: добавление/изменение количества, удаление, подсчет суммы.
- Заказы: создание заказа и просмотр истории.
- Создание товаров через `/admin/items/new`.
- Регистрация пользователя, login, logout.

Анонимный пользователь может смотреть список товаров и страницы товаров. Корзина, заказы, покупка доступны только после авторизации.

## Пользователи и безопасность

Пользователи хранятся в таблице `users`:

- `user_id`
- `username`
- `password` (BCrypt-хеш)

Для тестового пользователя в `data.sql` добавлен:

- логин: `testuser`
- пароль: `password`

Роли в приложении не разделяются. Любой авторизованный пользователь получает доступ ко всем защищенным действиям.

## Данные пользователей

Корзины и заказы изолированы по пользователям:

- `cart_item_counts` использует составной ключ `user_id + item_id`.
- `orders` содержит колонку `user_id`.
- пользователь видит и изменяет только свою корзину;
- пользователь видит только свои заказы;
- покупка выполняется только из корзины текущего пользователя.

## Платежный сервис

`payment-service` предоставляет API:

- `GET /balance` - получить баланс пользователя.
- `POST /payment` - списать сумму с баланса пользователя.

Оба эндпоинта требуют:

- `Authorization: Bearer <token>` - service-token от `market-app`;
- `X-User-Id: <id>` - id текущего пользователя витрины.

Баланс хранится в памяти платежного сервиса в `ConcurrentHashMap`: ключ - id пользователя, значение - текущий баланс. Если пользователь обращается впервые, ему назначается баланс `1000`.

Это учебная/игрушечная реализация. При перезапуске `payment-service` балансы сбрасываются.

## Service-to-Service авторизация

`market-app` ходит в `payment-service` с OAuth2 client credentials токеном. Токен добавляется в `PaymentClientConfig` через `WebClient` filter.

`payment-service` настроен как OAuth2 Resource Server и проверяет JWT через issuer:

```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri
```

В Docker Compose для этого поднимается сервер авторизации Keycloak.

## Redis

В Redis кешируются:

- товары по ключу `item:{id}`;
- страницы списка товаров `items:page={page}:size={size}[:search=...][:sort=...]`;
- изображения товаров `image:{id}` как Redis Hash с полями `contentType` и `bytes`.

Кеш используется только в `market-app`.

## Конфигурация

Переменные окружения для `market-app`:

- `R2DBC_URL` - URL PostgreSQL, по умолчанию `r2dbc:postgresql://localhost:5432/market`.
- `DB_USERNAME` - пользователь БД, по умолчанию `market_user`.
- `DB_PASSWORD` - пароль БД, по умолчанию `market_password`.
- `REDIS_HOST` - хост Redis, по умолчанию `localhost`.
- `REDIS_PORT` - порт Redis, по умолчанию `6379`.
- `PAYMENT_BASE_URL` - URL платежного сервиса, по умолчанию `http://localhost:8081`.
- `KEYCLOAK_ISSUER_URI` - issuer Keycloak, по умолчанию `http://localhost:8082/realms/master`.
- `KEYCLOAK_CLIENT_ID` - client id для client credentials, по умолчанию `market-client`.
- `KEYCLOAK_CLIENT_SECRET` - client secret для client credentials.

Переменные окружения для `payment-service`:

- `KEYCLOAK_ISSUER_URI` - issuer Keycloak, по умолчанию `http://localhost:8082/realms/master`.

В `market-app/src/main/resources/application.yaml` SQL-инициализация по умолчанию выключена:

```yaml
spring.sql.init.mode: never
```

Для локального запуска с пустой БД нужно либо заранее применить `schema.sql` и `data.sql`, либо запустить приложение с `--spring.sql.init.mode=always`.

## Локальный запуск

1. Поднимите PostgreSQL, Redis и Keycloak.

При настройке Keycloak нужно создать client для `market-app` с включенным client credentials flow. Его `client id` и `client secret` нужно передать в `KEYCLOAK_CLIENT_ID` и `KEYCLOAK_CLIENT_SECRET`.

2. Запустите платежный сервис:

```bash
./mvnw -pl payment-service spring-boot:run
```

3. Запустите витрину:

```bash
./mvnw -pl market-app spring-boot:run
```

Адреса по умолчанию:

- `market-app`: `http://localhost:8080`
- `payment-service`: `http://localhost:8081`
- Keycloak в Docker Compose: `http://localhost:8082`

На Windows можно использовать `.\mvnw.cmd` вместо `./mvnw`.

## Docker Compose

Создайте `.env` на основе `.env.example`, затем выполните:

```bash
docker compose up --build
```

Будут подняты:

- PostgreSQL
- Redis
- Keycloak
- `market-app`
- `payment-service`

При первом запуске Docker Compose в Keycloak будет автоматически зарегистрирован client для `market-app` по значениям из `.env`.

Остановка:

```bash
docker compose down
```

Чтобы удалить volume PostgreSQL вместе с данными:

```bash
docker compose down -v
```

## Тесты

Запуск всех тестов:

```bash
./mvnw test
```

`market-app` в e2e-тестах использует Testcontainers для PostgreSQL и Redis. Платежный клиент мокается.

## Сборка

```bash
./mvnw -DskipTests package
```

Собираются оба модуля.
