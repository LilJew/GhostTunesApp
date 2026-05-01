#!/usr/bin/env bash
# deploy.sh — упаковывает проект и деплоит на сервер по SSH
# Использование: ./deploy.sh user@your-server-ip

set -euo pipefail

# ── Параметры ─────────────────────────────────────────────────────────────────
REMOTE="${1:-}"
DEPLOY_DIR="/opt/ghosttunes"
ARCHIVE="ghosttunes-deploy.tar.gz"

# Публичный репозиторий — используется на сервере если Gitea недоступна
# Замени на свой GitHub/GitLab URL при публикации
GIT_REPO="https://192.168.0.173:3000/kirusha/GhostTunes.git"

if [[ -z "$REMOTE" ]]; then
  echo "Использование: ./deploy.sh user@server-ip"
  echo "Пример:        ./deploy.sh root@95.163.12.45"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

log()  { echo -e "\033[1;34m[deploy]\033[0m $*"; }
ok()   { echo -e "\033[1;32m[ok]\033[0m $*"; }
err()  { echo -e "\033[1;31m[error]\033[0m $*"; exit 1; }

# ── 1. Проверить наличие .env ─────────────────────────────────────────────────
if grep -q "change-me" .env 2>/dev/null; then
  err ".env содержит незаменённые placeholder-значения (change-me-*).\nОтредактируй .env перед деплоем."
fi

if grep -q "change-me" admin/.env.production 2>/dev/null; then
  err "admin/.env.production содержит незаменённые placeholder-значения.\nОтредактируй его перед деплоем."
fi

# ── 2. Упаковать проект ───────────────────────────────────────────────────────
log "Упаковываю проект..."
tar -czf "/tmp/$ARCHIVE" \
  --exclude=".git" \
  --exclude="android" \
  --exclude="*/node_modules" \
  --exclude="*/__pycache__" \
  --exclude="*/.gradle" \
  --exclude="*/build" \
  --exclude="*.pyc" \
  --exclude=".env.local" \
  -C "$SCRIPT_DIR" .
ok "Архив создан: /tmp/$ARCHIVE ($(du -sh /tmp/$ARCHIVE | cut -f1))"

# ── 3. Отправить архив на сервер ──────────────────────────────────────────────
log "Отправляю архив на $REMOTE..."
scp "/tmp/$ARCHIVE" "$REMOTE:/tmp/$ARCHIVE"
ok "Архив загружен"

# ── 4. Выполнить установку на сервере ─────────────────────────────────────────
log "Запускаю установку на сервере..."
ssh -t "$REMOTE" GIT_REPO="$GIT_REPO" DEPLOY_DIR="$DEPLOY_DIR" ARCHIVE="$ARCHIVE" bash <<'ENDSSH'
set -euo pipefail

log()  { echo -e "\033[1;34m[server]\033[0m $*"; }
ok()   { echo -e "\033[1;32m[ok]\033[0m $*"; }

# Установить Docker если не установлен
if ! command -v docker &>/dev/null; then
  log "Устанавливаю Docker..."
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
  ok "Docker установлен"
else
  ok "Docker уже установлен ($(docker --version | cut -d' ' -f3 | tr -d ','))"
fi

# Установить Docker Compose plugin если нет
if ! docker compose version &>/dev/null 2>&1; then
  log "Устанавливаю Docker Compose plugin..."
  apt-get update -qq && apt-get install -y -qq docker-compose-plugin
  ok "Docker Compose установлен"
else
  ok "Docker Compose уже установлен"
fi

# Создать директории
log "Создаю директории..."
mkdir -p "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR/certbot/conf"
mkdir -p "$DEPLOY_DIR/certbot/www"
mkdir -p /var/music/tracks
mkdir -p /var/music/covers

# Сохранить .env если уже есть
if [[ -f "$DEPLOY_DIR/.env" ]]; then
  cp "$DEPLOY_DIR/.env" "/tmp/.env.backup"
  log ".env уже существует — сохранён как /tmp/.env.backup"
fi

# Попробовать клонировать из git, иначе распаковать архив
if git ls-remote "$GIT_REPO" &>/dev/null 2>&1; then
  log "Gitea доступна — клонирую из репозитория..."
  if [[ -d "$DEPLOY_DIR/.git" ]]; then
    git -C "$DEPLOY_DIR" pull --rebase
  else
    rm -rf "$DEPLOY_DIR"
    git clone "$GIT_REPO" "$DEPLOY_DIR"
  fi
  ok "Репозиторий обновлён из git"
  rm -f "/tmp/$ARCHIVE"
else
  log "Gitea недоступна — распаковываю архив..."
  tar -xzf "/tmp/$ARCHIVE" -C "$DEPLOY_DIR"
  rm "/tmp/$ARCHIVE"
  ok "Архив распакован"
fi

# Восстановить .env если был
if [[ -f "/tmp/.env.backup" ]]; then
  cp "/tmp/.env.backup" "$DEPLOY_DIR/.env"
  ok ".env восстановлен из резервной копии"
fi

# Права на скрипт
chmod +x "$DEPLOY_DIR/deploy.sh" 2>/dev/null || true

# Проверить наличие SSL-сертификата
cd "$DEPLOY_DIR"
if [[ ! -f "certbot/conf/live/ghosttune.pro/fullchain.pem" ]]; then
  log "SSL-сертификат не найден. Получаю от Let's Encrypt..."
  log "Нужно убедиться что DNS ghosttune.pro и admin.ghosttune.pro указывают на этот сервер."
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

# Запустить/обновить контейнеры
log "Собираю и запускаю контейнеры..."
cd "$DEPLOY_DIR"
docker compose pull --quiet 2>/dev/null || true
docker compose up -d --build --remove-orphans

# Подождать и показать статус
sleep 5
echo ""
log "Статус контейнеров:"
docker compose ps

# Добавить cron для обновления сертификата если ещё нет
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
ENDSSH

# ── 5. Убрать локальный архив ─────────────────────────────────────────────────
rm "/tmp/$ARCHIVE"
ok "Готово"
