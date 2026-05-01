#!/usr/bin/env bash
# deploy-local.sh — запускается прямо на сервере, без SSH
# Использование: ./deploy-local.sh

set -euo pipefail

DEPLOY_DIR="/opt/ghosttunes"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log()  { echo -e "\033[1;34m[deploy]\033[0m $*"; }
ok()   { echo -e "\033[1;32m[ok]\033[0m $*"; }
err()  { echo -e "\033[1;31m[error]\033[0m $*"; exit 1; }

# ── 1. Проверить наличие .env ─────────────────────────────────────────────────
if grep -q "change-me" "$SCRIPT_DIR/.env" 2>/dev/null; then
  err ".env содержит незаменённые placeholder-значения (change-me-*).\nОтредактируй .env перед деплоем."
fi

if grep -q "change-me" "$SCRIPT_DIR/admin/.env.production" 2>/dev/null; then
  err "admin/.env.production содержит незаменённые placeholder-значения.\nОтредактируй его перед деплоем."
fi

# ── 2. Установить Docker если не установлен ───────────────────────────────────
if ! command -v docker &>/dev/null; then
  log "Устанавливаю Docker..."
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
  ok "Docker установлен"
else
  ok "Docker уже установлен ($(docker --version | cut -d' ' -f3 | tr -d ','))"
fi

# ── 3. Установить Docker Compose plugin если нет ──────────────────────────────
if ! docker compose version &>/dev/null 2>&1; then
  log "Устанавливаю Docker Compose plugin..."
  apt-get update -qq && apt-get install -y -qq docker-compose-plugin
  ok "Docker Compose установлен"
else
  ok "Docker Compose уже установлен"
fi

# ── 4. Создать директории ─────────────────────────────────────────────────────
log "Создаю директории..."
mkdir -p "$DEPLOY_DIR/certbot/conf"
mkdir -p "$DEPLOY_DIR/certbot/www"
mkdir -p /var/music/tracks
mkdir -p /var/music/covers

# ── 5. Скопировать файлы проекта если запускается не из DEPLOY_DIR ────────────
if [[ "$SCRIPT_DIR" != "$DEPLOY_DIR" ]]; then
  log "Копирую файлы из $SCRIPT_DIR в $DEPLOY_DIR..."
  if [[ -f "$DEPLOY_DIR/.env" ]]; then
    cp "$DEPLOY_DIR/.env" "/tmp/.env.backup"
    log ".env уже существует — сохранён как /tmp/.env.backup"
  fi
  cp -a "$SCRIPT_DIR/." "$DEPLOY_DIR/"
  rm -rf "$DEPLOY_DIR/.git" \
         "$DEPLOY_DIR/android" \
         "$DEPLOY_DIR/admin/node_modules" \
         "$DEPLOY_DIR/frontend/node_modules"
  if [[ -f "/tmp/.env.backup" ]]; then
    cp "/tmp/.env.backup" "$DEPLOY_DIR/.env"
    ok ".env восстановлен из резервной копии"
  fi
  ok "Файлы скопированы"
fi

# ── 6. Проверить наличие SSL-сертификата ──────────────────────────────────────
cd "$DEPLOY_DIR"
if [[ ! -f "certbot/conf/live/ghosttune.pro/fullchain.pem" ]]; then
  log "SSL-сертификат не найден. Получаю от Let's Encrypt..."
  log "Убедитесь что DNS ghosttune.pro и admin.ghosttune.pro указывают на этот сервер."
  echo ""
  read -rp "  Продолжить получение сертификата? [y/N]: " confirm
  if [[ "$confirm" =~ ^[Yy]$ ]]; then
    read -rp "  Email для Let's Encrypt: " le_email
    docker run --rm \
      -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
      -v "$(pwd)/certbot/www:/var/www/certbot" \
      -p 80:80 \
      certbot/certbot certonly --standalone \
      -d ghosttune.pro -d www.ghosttune.pro -d admin.ghosttune.pro \
      --email "$le_email" --agree-tos --no-eff-email
    ok "Сертификат получен"
  else
    log "Пропускаю. Запусти вручную: см. DEPLOY.md"
    log "Nginx не запустится без сертификата."
  fi
else
  ok "SSL-сертификат уже есть"
fi

# ── 7. Собрать и запустить контейнеры ────────────────────────────────────────
log "Собираю и запускаю контейнеры..."
cd "$DEPLOY_DIR"
docker compose pull --quiet 2>/dev/null || true
docker compose up -d --build --remove-orphans

sleep 5
echo ""
log "Статус контейнеров:"
docker compose ps

# ── 8. Cron для автообновления сертификата ────────────────────────────────────
if ! crontab -l 2>/dev/null | grep -q "certbot"; then
  (crontab -l 2>/dev/null; echo "0 3 * * * cd $DEPLOY_DIR && docker run --rm -v \$(pwd)/certbot/conf:/etc/letsencrypt -v \$(pwd)/certbot/www:/var/www/certbot certbot/certbot renew --webroot -w \$(pwd)/certbot/www --quiet && docker compose exec nginx nginx -s reload 2>/dev/null || true") | crontab -
  ok "Cron для автообновления сертификата добавлен"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Деплой завершён!"
echo "  Плеер:    https://ghosttune.pro"
echo "  API docs: https://ghosttune.pro/api/v1/docs"
echo "  Admin:    https://admin.ghosttune.pro"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
