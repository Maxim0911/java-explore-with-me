# Explore With Me 🗺️

Приложение для поиска и организации событий, позволяющее пользователям делиться информацией об интересных мероприятиях, оставлять комментарии и находить компанию для участия.

## 🚀 Функциональность

### Основной сервис (Main Service)
- **Публичный API**: просмотр событий, категорий, подборок
- **Приватный API**: создание и управление событиями, подача заявок на участие
- **Административный API**: управление пользователями, категориями, событиями, подборками
- **Комментарии к событиям**: пользователи могут оставлять, редактировать и удалять комментарии, администраторы управляют модерацией

### Сервис статистики (Stats Service)
- Сохранение информации о просмотрах событий
- Получение статистики по посещениям

## 🛠 Технологии

| Технология | Версия |
|------------|--------|
| Java | 21 |
| Spring Boot | 3.3.2 |
| Spring Data JPA | - |
| Spring Validation | - |
| PostgreSQL | 16.1 |
| Docker & Docker Compose | - |
| Maven | - |
| Lombok | 1.18.34 |
| MapStruct | 1.6.0 |
| OpenAPI 3.0 (Springdoc) | 2.6.0 |

## 📦 Запуск приложения

### Предварительные требования
- Docker и Docker Compose
- Java 21 (для локального запуска без Docker)
- Maven (для локальной сборки)

### Запуск с Docker Compose

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd java-explore-with-me
```

2. **Соберите проект:**
```bash
mvn clean package -DskipTests
```
3. **Запустите сервисы:** 
```bash
   docker-compose up --build
```

Сервисы будут доступны по адресам:

Основной сервис: http://localhost:8080

Сервис статистики: http://localhost:9090

Базы данных PostgreSQL: порты 5432 (stats-db) и 5433 (ewm-db)

## 📚 API Документация

### Swagger UI

| Сервис | URL |
|--------|-----|
| Основной сервис | http://localhost:8080/swagger-ui/index.html |
| Сервис статистики | http://localhost:9090/swagger-ui/index.html |

### OpenAPI JSON

| Сервис | URL |
|--------|-----|
| Основной сервис | http://localhost:8080/v3/api-docs |
| Сервис статистики | http://localhost:9090/v3/api-docs |

---

### 📖 Публичный API (Public)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | /categories | Получение списка категорий |
| GET | /categories/{catId} | Получение категории по ID |
| GET | /compilations | Получение списка подборок |
| GET | /compilations/{compId} | Получение подборки по ID |
| GET | /events | Поиск событий с фильтрацией |
| GET | /events/{id} | Получение события по ID |
| GET | /events/{eventId}/comments | Получение комментариев события |

---

### 🔒 Приватный API (Private)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /users/{userId}/events | Создание нового события |
| PATCH | /users/{userId}/events/{eventId} | Обновление своего события |
| GET | /users/{userId}/events | Получение всех событий пользователя |
| GET | /users/{userId}/events/{eventId} | Получение события пользователя по ID |
| GET | /users/{userId}/events/{eventId}/requests | Получение заявок на участие в событии |
| PATCH | /users/{userId}/events/{eventId}/requests | Обновление статуса заявок |
| GET | /users/{userId}/requests | Получение заявок пользователя на участие |
| POST | /users/{userId}/requests | Создание заявки на участие |
| PATCH | /users/{userId}/requests/{requestId}/cancel | Отмена заявки на участие |
| POST | /users/{userId}/events/{eventId}/comments | Создание комментария к событию |
| PATCH | /users/{userId}/comments/{commentId} | Редактирование своего комментария |
| DELETE | /users/{userId}/comments/{commentId} | Удаление своего комментария |
| GET | /users/{userId}/comments | Получение всех своих комментариев |

---

### 👑 Административный API (Admin)

#### Категории

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /admin/categories | Создание категории |
| PATCH | /admin/categories/{catId} | Обновление категории |
| DELETE | /admin/categories/{catId} | Удаление категории |

#### Пользователи

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /admin/users | Создание пользователя |
| GET | /admin/users | Получение списка пользователей |
| DELETE | /admin/users/{userId} | Удаление пользователя |

#### События

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | /admin/events | Поиск событий с фильтрацией |
| PATCH | /admin/events/{eventId} | Редактирование события (включая публикацию) |

#### Подборки

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /admin/compilations | Создание подборки |
| PATCH | /admin/compilations/{compId} | Обновление подборки |
| DELETE | /admin/compilations/{compId} | Удаление подборки |

#### Комментарии (Модерация)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | /admin/comments | Получение всех комментариев (с фильтрацией) |
| PATCH | /admin/comments/{commentId}/publish | Публикация комментария |
| PATCH | /admin/comments/{commentId}/reject | Отклонение комментария |
| PUT | /admin/comments/{commentId} | Редактирование комментария (админ) |
| DELETE | /admin/comments/{commentId} | Удаление комментария (админ, жесткое) |

---

### 📊 Сервис статистики (Stats API)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /hit | Сохранение информации о запросе |
| GET | /stats | Получение статистики по посещениям |




https://github.com/Maxim0911/java-explore-with-me/pull/5