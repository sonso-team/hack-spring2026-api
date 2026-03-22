# DDoS-Ninja — API

Серверная часть проекта «DDoS-Ninja». Хакатон ДГТУ Весна 2026.

## Стек

- Kotlin
- Spring Boot
- Gradle
- Docker Compose

## Запуск (Docker)

```bash
docker compose up --build
```

## Запуск без Docker

```bash
./gradlew bootRun
```

## Структура

```
src/main/
├── kotlin/          — исходный код
│   ├── auth/        — авторизация, JWT, роли
│   ├── lobby/       — создание, toggle, удаление лобби
│   ├── player/      — регистрация игрока, проверка статуса
│   ├── game/        — start, finish, валидация очков
│   └── admin/       — управление админами (суперадмин)
└── resources/       — конфигурация, application.yml
```

## API

Полная документация: `openapi.yaml` в корне репозитория.

| Группа | Префикс | Описание |
|---|---|---|
| Auth | `/api/auth` | Логин, JWT |
| Lobby | `/api/admin/lobby` | CRUD лобби, QR, toggle статуса |
| Results | `/api/admin/lobby/results` | Таблица, экспорт CSV, рандомайзер |
| Admins | `/api/admin/admins` | Список, добавление, удаление (суперадмин) |
| Player | `/api/play` | Регистрация, статус игрока 

## Ключевые особенности

- Одновременно активно только одно лобби
- Два запроса на игру: start → finish 
- Роли: суперадмин (seed) + админ (через интерфейс)
- Уникальность игрока: phone + lobby_id

## Фронтенд

[hack-spring2026-front](https://github.com/sonso-team/hack-spring2026-front)

## Команда

sonso-team — Хакатон ДГТУ Весна 2026