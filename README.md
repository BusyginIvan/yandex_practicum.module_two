# Витрина интернет-магазина (Spring Boot)

Учебный проект. Веб-приложение витрины товаров с корзиной, оформлением заказа и историей заказов.

## Технологии

- Java 21
- Spring Boot
- Spring Web MVC (блокирующий стек)
- Thymeleaf
- Spring Data JPA + Hibernate ORM
- PostgreSQL
- Maven
- JUnit 5, Spring Boot Test, MockMvc, Testcontainers
- Docker / Docker Compose

## Функциональность

- Витрина товаров:
  - просмотр товаров;
  - поиск по названию/описанию;
  - сортировка (`NO`, `ALPHA`, `PRICE`);
  - пагинация (`2, 5, 10, 20, 50, 100`);
  - изменение количества товара в корзине.
- Карточка товара:
  - просмотр информации о товаре;
  - изменение количества товара в корзине.
- Корзина:
  - список выбранных товаров;
  - изменение количества и удаление;
  - подсчёт общей суммы;
  - оформление заказа.
- Заказы:
  - список всех заказов;
  - страница отдельного заказа.
- Дополнительно:
  - загрузка изображений товаров;
  - форма добавления товара: `/admin/items/new`.

## Основные эндпоинты

- `GET /` и `GET /items`
- `POST /items`
- `GET /items/{id}`
- `POST /items/{id}`
- `GET /cart/items`
- `POST /cart/items`
- `GET /orders`
- `GET /orders/{id}`
- `POST /buy`
- `GET /images/{id}`

## Структура проекта

- `src/main/java/.../api/controller` — MVC-контроллеры
- `src/main/java/.../service` — бизнес-логика
- `src/main/java/.../persistence/entity` — JPA-сущности
- `src/main/java/.../persistence/repository` — Spring Data репозитории
- `src/main/resources/templates` — Thymeleaf-шаблоны
- `src/main/resources/schema.sql` — схема БД
- `src/test/java` — unit/integration/e2e тесты

## Переменные окружения

Приложение читает:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Для `docker-compose` вместо `DB_URL` используется `DB_NAME`.

## Запуск локально

1. Поднимите PostgreSQL и создайте БД.
2. Заполните переменные окружения (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
3. Выполните:

```bash
mvn clean package
java -jar target/market-app-1.0-SNAPSHOT.jar
```

Приложение будет доступно на `http://localhost:8080`.

## Запуск в Docker

Запуск приложения и PostgreSQL:

```bash
docker compose up --build
```

После старта приложение доступно на `http://localhost:8080`.

Остановка:

```bash
docker compose down
```

## Тестирование

Запуск всех тестов:

```bash
mvn clean test
```

В проекте есть:

- сервисные тесты;
- интеграционный e2e-тест с `@SpringBootTest + MockMvc + Testcontainers`.

## Сборка артефакта

Executable JAR:

```bash
mvn -DskipTests package
```

Артефакт: `target/market-app-1.0-SNAPSHOT.jar`.
