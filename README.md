# GhostTunes

Full-stack музыкальное приложение с админ-панелью

## Структура проекта

```
C:\ghosttunes\
├── start.py          # Главный скрипт запуска
├── start.bat         # Windows запуск
├── README.md         # Этот файл
├── backend/          # FastAPI + MySQL
├── admin/            # React админ-панель
└── ...              # Другие файлы
```

## Быстрый старт

1. Установите [Python 3.8+](https://python.org)
2. Установите [Docker Desktop](https://www.docker.com/products/docker-desktop/)
3. Запустите приложение:
   ```bash
   python start.py
   ```
   Или через батник:
   ```bash
   start.bat
   ```

## Доступные сервисы

- **API Backend**: http://localhost:8000
- **Swagger API**: http://localhost:8000/docs
- **Admin Panel**: http://localhost:3000 (логин: admin / admin)
- **Android эмулятор**: http://10.0.2.2:8000/api/v1/

## Технологии

### Backend
- **FastAPI** - Python веб-фреймворк
- **MySQL** - база данных
- **Alembic** - миграции БД
- **Docker** - контейнеризация

### Admin Panel
- **React** - фронтенд
- **Docker** - контейнеризация

## Запуск компонентов

### Backend
```bash
cd backend
docker compose up -d --build
```

### Admin Panel
```bash
cd admin
docker compose up -d --build
```

## Docker требования

- Docker Desktop должен быть запущен
- Должен быть готов к работе (иконка в трее зеленая)

## Проблемы

1. **Docker не запускается**:
   - Убедитесь, что Docker Desktop установлен
   - Запустите Docker Desktop с правами администратора
   - Дождитесь полной инициализации (иконка станет зеленой)

2. **MySQL не готов**:
   - Дождитесь полной загрузки контейнеров
   - Проверьте логи: `cd backend && docker compose logs`

3. **Admin Panel не открывается**:
   - Убедитесь, что порт 3000 доступен
   - Проверьте статус контейнеров: `cd admin && docker compose ps`

## Разработка

### Backend
- Файлы API: `backend/app/`
- Модели: `backend/models/`
- Миграции: `backend/alembic/`

### Frontend
- Компоненты: `admin/src/`
- Стили: `admin/src/styles/`

## Лицензия

[MIT License](LICENSE)