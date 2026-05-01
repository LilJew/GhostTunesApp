# Деплой на ghosttunes.pro

Проект деплоится с локальной машины одной командой через `deploy.sh`.
Скрипт сам устанавливает Docker, копирует файлы, получает SSL-сертификат и запускает контейнеры.

---

## Быстрый старт

### 1. Настроить секреты

Перед первым деплоем заменить все `change-me-*` в двух файлах:

**`.env`** — секреты бэкенда:
```bash
nano .env
```
```
MYSQL_ROOT_PASSWORD=ваш-пароль-root
MYSQL_PASSWORD=ваш-пароль-бд
SECRET_KEY=длинная-случайная-строка   # python3 -c "import secrets; print(secrets.token_hex(32))"
API_KEY=ваш-api-ключ
ADMIN_LOGIN=admin
ADMIN_PASSWORD=ваш-пароль-админки
```

**`admin/.env.production`** — конфиг сборки фронтенда:
```bash
nano admin/.env.production
```
```
VITE_API_BASE=https://ghosttunes.pro/api/v1
VITE_API_KEY=ваш-api-ключ             # тот же что API_KEY в .env
VITE_ADMIN_LOGIN=admin
VITE_ADMIN_PASS=ваш-пароль-админки    # тот же что ADMIN_PASSWORD в .env
```

### 2. Запустить deploy.sh

**Git Bash / WSL / Linux / macOS:**
```bash
./deploy.sh root@95.163.12.45
```

**Windows (cmd/PowerShell):**
```bat
deploy.bat root@95.163.12.45
```

Скрипт попросит подтвердить получение SSL-сертификата и email для Let's Encrypt.
После этого всё запустится автоматически.

---

## Что делает deploy.sh

```
[локально]
  1. Проверяет что в .env и admin/.env.production нет change-me-*
  2. Упаковывает проект в tar.gz (без node_modules, .git, android)
  3. Отправляет архив на сервер по SCP

[на сервере]
  4. Устанавливает Docker + Docker Compose (если не установлены)
  5. Создаёт директории /opt/ghosttunes, /var/music/tracks|covers, certbot/
  6. Сохраняет существующий .env (при повторном деплое)
  7. Получает код: пробует git clone/pull из GIT_REPO,
     если Gitea недоступна — распаковывает загруженный архив
  8. Восстанавливает .env
  9. Получает SSL-сертификат Let's Encrypt (интерактивно, только при первом деплое)
  10. docker compose up -d --build
  11. Добавляет cron для автообновления сертификата (03:00 каждую ночь)
```

---

## Повторный деплой (обновление)

Просто запустить ту же команду заново:
```bash
./deploy.sh root@95.163.12.45
```

- `.env` на сервере **не перезапишется** — сохраняется автоматически
- Если Gitea доступна — делает `git pull --rebase`, иначе загружает новый архив
- Пересобирает только изменившиеся слои Docker

---

## Требования

| Что | Где |
|-----|-----|
| SSH-доступ к серверу | локальная машина |
| `scp` и `ssh` в PATH | локальная машина |
| Git Bash (Windows) | локальная машина |
| Ubuntu 20.04+ / Debian 11+ | сервер |
| Открытые порты 80, 443 | сервер |
| DNS A-записи → IP сервера | регистратор домена |

DNS должен быть настроен **до** запуска скрипта (нужен для SSL):
```
ghosttunes.pro       A  →  IP-сервера
www.ghosttunes.pro   A  →  IP-сервера
admin.ghosttunes.pro A  →  IP-сервера
```

---

## Смена репозитория

Если Gitea локальная и недоступна с сервера — опубликуй репо на GitHub и обнови переменную в `deploy.sh`:

```bash
# deploy.sh, строка 14:
GIT_REPO="https://github.com/твой-юзернейм/GhostTunes.git"
```

---

## Ручной деплой (без скрипта)

Если нужно развернуть вручную прямо на сервере:

```bash
# 1. Клонировать
git clone <repo> /opt/ghosttunes
cd /opt/ghosttunes

# 2. Заполнить .env
cp .env.example .env   # или создать вручную
nano .env

# 3. Получить SSL (порт 80 должен быть свободен)
docker run --rm \
  -v $(pwd)/certbot/conf:/etc/letsencrypt \
  -v $(pwd)/certbot/www:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  -d ghosttunes.pro -d www.ghosttunes.pro -d admin.ghosttunes.pro \
  --email your@email.com --agree-tos --no-eff-email

# 4. Запустить
docker compose up -d --build
```

---

## Диагностика

```bash
# Статус контейнеров
docker compose -f /opt/ghosttunes/docker-compose.yml ps

# Логи всех сервисов
docker compose -f /opt/ghosttunes/docker-compose.yml logs -f

# Логи конкретного сервиса
docker compose -f /opt/ghosttunes/docker-compose.yml logs -f nginx
docker compose -f /opt/ghosttunes/docker-compose.yml logs -f api

# Перезапустить один сервис
docker compose -f /opt/ghosttunes/docker-compose.yml restart api
```

---

## URLs после деплоя

| URL | Назначение |
|-----|-----------|
| `https://ghosttunes.pro` | Веб-плеер |
| `https://ghosttunes.pro/api/v1/docs` | Swagger API |
| `https://admin.ghosttunes.pro` | Панель администратора |
