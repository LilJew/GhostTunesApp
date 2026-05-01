"""
GhostTunes Full Stack Launcher v2
Структура папок:
  C:\ghosttunes\
  ├── start.py      <- этот файл
  ├── start.bat
  ├── backend\      <- FastAPI + MySQL
  └── admin\        <- React админка
"""

import subprocess
import sys
import time
import os
import shutil
import webbrowser


class C:
    RED    = "\033[91m"
    GREEN  = "\033[92m"
    YELLOW = "\033[93m"
    CYAN   = "\033[96m"
    BOLD   = "\033[1m"
    RESET  = "\033[0m"


def ok(msg):         print(f"  {C.GREEN}✓{C.RESET}  {msg}")
def fail(msg):       print(f"  {C.RED}✗{C.RESET}  {C.RED}{msg}{C.RESET}")
def info(msg):       print(f"  {C.CYAN}→{C.RESET}  {msg}")
def warn(msg):       print(f"  {C.YELLOW}!{C.RESET}  {C.YELLOW}{msg}{C.RESET}")
def step(n, t, msg): print(f"\n{C.BOLD}[{n}/{t}]{C.RESET} {msg}")


def run(cmd, cwd=None, capture=False):
    result = subprocess.run(
        cmd, shell=True, cwd=cwd,
        stdout=subprocess.PIPE if capture else None,
        stderr=subprocess.PIPE if capture else None,
    )
    return result.returncode, (result.stdout or b"").decode().strip()


HERE        = os.path.dirname(os.path.abspath(__file__))
BACKEND_DIR = os.path.join(HERE, "backend")
ADMIN_DIR   = os.path.join(HERE, "admin")
TOTAL       = 6


def banner():
    os.system("cls" if os.name == "nt" else "clear")
    print(f"""{C.CYAN}{C.BOLD}
  ██████  ██   ██  ██████  ███████ ████████ ████████ ██    ██ ███    ██ ███████ ███████
 ██       ██   ██ ██    ██ ██         ██       ██    ██    ██ ████   ██ ██      ██
 ██   ███ ███████ ██    ██ ███████    ██       ██    ██    ██ ██ ██  ██ █████   ███████
 ██    ██ ██   ██ ██    ██      ██    ██       ██    ██    ██ ██  ██ ██ ██           ██
  ██████  ██   ██  ██████  ███████    ██       ██     ██████  ██   ████ ███████ ███████
{C.RESET}
  {C.BOLD}Full Stack Launcher{C.RESET}  —  Backend + Admin Panel
  {"─" * 54}
""")


def check_docker():
    step(1, TOTAL, "Проверяем Docker...")
    code, _ = run("docker info", capture=True)
    if code != 0:
        info("Docker не запущен. Проверяем Docker Desktop...")
        
        # Сначала проверяем, установлен ли Docker
        code, _ = run("docker --version", capture=True)
        if code != 0:
            fail("Docker не установлен.")
            info("Установите Docker Desktop с: https://www.docker.com/products/docker-desktop/")
            return False
        
        info("Docker установлен, но не запущен.")
        
        # Метод 1: Проверяем, уже ли запущен процесс Docker Desktop
        info("Проверяем запущенные процессы Docker Desktop...")
        code, _ = run("powershell -Command \"Get-Process 'Docker Desktop' -ErrorAction SilentlyContinue\"", capture=True)
        if code == 0:
            info("Docker Desktop уже запущен, проверяем готовность...")
            # Проверяем готовность с таймаутом
            for attempt in range(1, 21):  # 20 * 3 = 60 секунд
                code, _ = run("docker info", capture=True)
                if code == 0:
                    ok(f"Docker готов (попытка {attempt}/20)")
                    return True
                else:
                    bar = "█" * min(attempt, 20) + "░" * max(0, 20 - attempt)
                    elapsed = attempt * 3
                    print(f"\r  Ожидание... [{bar}] {elapsed}s ", end="", flush=True)
                    time.sleep(3)
            print()
            warn("Docker Desktop запущен, но демон не готов за 60 секунд")
        
        # Метод 2: Запускаем Docker Desktop с правами администратора (без ожидания)
        info("Запускаем Docker Desktop с правами администратора...")
        code, _ = run("powershell -Command \"Start-Process 'Docker Desktop' -Verb RunAs -WindowStyle Hidden\"", capture=True)
        if code == 0:
            info("Docker Desktop запущен в фоновом режиме")
        else:
            warn("Не удалось запустить Docker Desktop")
        
        # Метод 3: Запускаем Docker Desktop напрямую (без ожидания)
        info("Пытаемся запустить Docker Desktop напрямую...")
        code, _ = run("powershell -Command \"Start-Process 'C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe' -Verb RunAs -WindowStyle Hidden\"", capture=True)
        if code != 0:
            warn("Прямой запуск не сработал")
        
        # Метод 4: Запускаем через Windows Service
        info("Проверяем службу Windows...")
        code, _ = run("sc query com.docker.service", capture=True)
        if code == 0:
            info("Запускаем службу Docker Desktop...")
            code, _ = run("sc start com.docker.service", capture=True)
            if code == 0:
                info("Служба запущена")
            else:
                warn("Не удалось запустить службу")
        else:
            warn("Служба Docker Desktop не найдена")
        
        # Ожидание Docker с прогресс-баром
        info("Ожидаем готовности Docker (максимум 2 минуты)...")
        attempts = 0
        max_attempts = 40  # 40 * 3 = 120 секунд (2 минуты)
        
        while attempts < max_attempts:
            code, _ = run("docker info", capture=True)
            if code == 0:
                print()
                ok(f"Docker готов (попытка {attempts + 1}/{max_attempts})")
                return True
            else:
                bar = "█" * min(attempts + 1, 30) + "░" * max(0, 30 - attempts - 1)
                elapsed = (attempts + 1) * 3
                print(f"\r  Ожидание Docker... [{bar}] {elapsed}s ", end="", flush=True)
                attempts += 1
                time.sleep(3)
        
        print()
        fail("Docker Desktop не готов за 2 минуты")
        
        # Проверяем, может быть Docker уже готов, но мы не видим
        info("Проверяем еще раз...")
        code, _ = run("docker ps", capture=True)
        if code == 0:
            ok("Docker контейнеры доступны, продолжаем...")
            return True
        
        # Если все еще не готов, выводим инструкции
        info("Docker Desktop запущен, но демон может быть не полностью инициализирован.")
        info("Попробуйте:")
        info("  1. Дождитесь полной загрузки Docker Desktop (иконка в трее станет зеленой)")
        info("  2. Перезапустите скрипт: python start.py")
        info("  3. Или нажмите Ctrl+C для выхода и запустите вручную")
        return False
    else:
        ok("Docker запущен")
        return True


def check_dirs():
    step(2, TOTAL, "Проверяем структуру папок...")
    ok(f"Корень: {HERE}")
    if not os.path.isdir(BACKEND_DIR):
        fail(f"Папка backend/ не найдена: {BACKEND_DIR}")
        return False
    ok("backend/ найден")
    if not os.path.isdir(ADMIN_DIR):
        warn("Папка admin/ не найдена — админка не запустится")
    else:
        ok("admin/ найден")
    env    = os.path.join(BACKEND_DIR, ".env")
    env_ex = os.path.join(BACKEND_DIR, ".env.example")
    if not os.path.exists(env):
        if os.path.exists(env_ex):
            shutil.copy(env_ex, env)
            ok("Создан backend/.env из .env.example")
        else:
            warn("backend/.env не найден")
    else:
        ok("backend/.env существует")
    return True


def docker_up_backend():
    step(3, TOTAL, "Запускаем бэкенд (MySQL + FastAPI)...")
    info("docker compose up -d --build  (1-3 мин при первом запуске...)")
    print()
    code, _ = run("docker compose up -d --build", cwd=BACKEND_DIR)
    print()
    if code != 0:
        fail("docker compose up завершился с ошибкой.")
        fail(f"Проверь: cd {BACKEND_DIR} && docker compose logs")
        return False
    ok("Контейнеры бэкенда запущены")
    return True


def wait_for_mysql(max_attempts=40, interval=3):
    step(4, TOTAL, "Ожидаем готовности MySQL...")
    for attempt in range(1, max_attempts + 1):
        code, _ = run(
            "docker compose exec -T db mysqladmin ping -h localhost -u root -proot --silent",
            cwd=BACKEND_DIR, capture=True,
        )
        if code == 0:
            print()
            ok(f"MySQL готов (попытка {attempt}/{max_attempts})")
            return True
        bar     = "█" * min(attempt, 30) + "░" * max(0, 30 - attempt)
        elapsed = attempt * interval
        print(f"\r  Ожидание... [{bar}] {elapsed}s ", end="", flush=True)
        time.sleep(interval)
    print()
    fail(f"MySQL не запустился за {max_attempts * interval} сек.")
    return False


def run_migrations():
    step(5, TOTAL, "Применяем миграции Alembic...")
    code, _ = run(
        "docker compose exec -T api alembic upgrade head",
        cwd=BACKEND_DIR, capture=True,
    )
    if code != 0:
        warn("Ошибка миграций (возможно уже применены — это нормально)")
    else:
        ok("Миграции применены")
    return True


def docker_up_admin():
    step(6, TOTAL, "Запускаем Admin Panel...")
    if not os.path.isdir(ADMIN_DIR):
        warn("Папка admin/ не найдена — пропускаем")
        return True
    if not os.path.exists(os.path.join(ADMIN_DIR, "docker-compose.yml")):
        warn("admin/docker-compose.yml не найден — пропускаем")
        return True
    code, _ = run("docker compose up -d --build", cwd=ADMIN_DIR)
    if code != 0:
        warn("Не удалось запустить Admin Panel автоматически")
        warn(f"Попробуй вручную: cd {ADMIN_DIR} && docker compose up -d --build")
    else:
        ok("Admin Panel запущена  →  http://localhost:5173")
    return True


def print_summary():
    admin_ok = os.path.isdir(ADMIN_DIR)
    print(f"""
  {"─" * 54}

  {C.GREEN}{C.BOLD}GhostTunes запущен!{C.RESET}

  {C.BOLD}Backend:{C.RESET}
     API     →  http://localhost:8000
     Swagger →  http://localhost:8000/docs

  {C.BOLD}Admin Panel:{C.RESET}
     {"→  http://localhost:5173  (логин: admin / admin)" if admin_ok else f"{C.YELLOW}не запущена{C.RESET}"}

  {C.BOLD}Android эмулятор:{C.RESET}
     →  http://10.0.2.2:8000/api/v1/

  {"─" * 54}
""")


def main():
    banner()

    for fn in [check_docker, check_dirs, docker_up_backend,
               wait_for_mysql, run_migrations, docker_up_admin]:
        if not fn():
            print()
            input("  Нажми Enter для выхода...")
            sys.exit(1)

    print_summary()

    try:
        print("  Что открыть в браузере?")
        print(f"  {C.BOLD}1{C.RESET} — Admin Panel   http://localhost:5173")
        print(f"  {C.BOLD}2{C.RESET} — Swagger API   http://localhost:8000/docs")
        print(f"  {C.BOLD}3{C.RESET} — Оба")
        print(f"  {C.BOLD}Enter{C.RESET} — ничего")
        choice = input("\n  Выбор: ").strip()
        if choice == "1":
            webbrowser.open("http://localhost:5173")
        elif choice == "2":
            webbrowser.open("http://localhost:8000/docs")
        elif choice == "3":
            webbrowser.open("http://localhost:5173")
            time.sleep(0.5)
            webbrowser.open("http://localhost:8000/docs")
    except (KeyboardInterrupt, EOFError):
        pass

    print()
    input("  Нажми Enter для выхода (всё продолжит работать)...")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print(f"\n\n  {C.YELLOW}Прервано.{C.RESET}\n")
        sys.exit(0)
