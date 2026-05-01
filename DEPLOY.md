# Деплой на ghosttunes.pro

## Требования на сервере
- Docker + Docker Compose v2
- Открытые порты 80 и 443
- DNS: A-записи `ghosttunes.pro` и `admin.ghosttunes.pro` → IP сервера

---

## 1. Загрузить проект на сервер

```bash
git clone <repo> /opt/ghosttunes
cd /opt/ghosttunes
```

---

## 2. Настроить секреты

Отредактировать `.env` — заменить все `change-me-*` на реальные значения:

```bash
nano .env
```

Отредактировать `admin/.env.production` — указать тот же `VITE_API_KEY` и `VITE_ADMIN_PASS`:

```bash
nano admin/.env.production
```

---

## 3. Получить SSL-сертификат (Let's Encrypt)

Сначала запустить nginx только на порту 80 (без HTTPS), закомментировав SSL-серверы в `nginx/nginx.conf`.
Затем запустить certbot:

```bash
docker run --rm \
  -v $(pwd)/certbot/conf:/etc/letsencrypt \
  -v $(pwd)/certbot/www:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  -d ghosttunes.pro -d www.ghosttunes.pro -d admin.ghosttunes.pro \
  --email your@email.com --agree-tos --no-eff-email
```

После получения сертификата — раскомментировать HTTPS-блоки в `nginx/nginx.conf`.

---

## 4. Запустить

```bash
docker compose up -d --build
```

Проверить статус:
```bash
docker compose ps
docker compose logs -f
```

---

## 5. Автообновление сертификата (cron)

```bash
crontab -e
# добавить:
0 3 * * * cd /opt/ghosttunes && docker run --rm \
  -v $(pwd)/certbot/conf:/etc/letsencrypt \
  -v $(pwd)/certbot/www:/var/www/certbot \
  certbot/certbot renew --webroot -w /var/www/certbot \
  && docker compose exec nginx nginx -s reload
```

---

## URLs после деплоя

| URL | Назначение |
|-----|-----------|
| `https://ghosttunes.pro` | Веб-плеер |
| `https://ghosttunes.pro/api/v1/docs` | Swagger API |
| `https://admin.ghosttunes.pro` | Панель администратора |

---

## Обновление

```bash
git pull
docker compose up -d --build
```
