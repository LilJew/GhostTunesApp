@echo off
chcp 65001 >nul
title GhostTunes Backend

:: Переходим в папку батника
cd /d "%~dp0"

:: Проверяем наличие Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo  ОШИБКА: Python не найден.
    echo  Скачай с https://python.org и установи.
    echo.
    pause
    exit /b 1
)

:: Запускаем Python скрипт
python start.py

:: Если скрипт упал — не закрываем окно
if %errorlevel% neq 0 (
    echo.
    echo  Скрипт завершился с ошибкой (код %errorlevel%)
    pause
)

:: Открываем веб интерфейс
echo.
echo  Веб интерфейс доступен: http://localhost:3000
echo.
start http://localhost:3000
pause