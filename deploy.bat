@echo off
:: Запускает deploy.sh через Git Bash
:: Использование: deploy.bat user@server-ip

if "%~1"=="" (
    echo Использование: deploy.bat user@server-ip
    echo Пример:        deploy.bat root@95.163.12.45
    exit /b 1
)

:: Найти Git Bash
set "BASH="
for %%p in (
    "%ProgramFiles%\Git\bin\bash.exe"
    "%ProgramFiles(x86)%\Git\bin\bash.exe"
    "%LocalAppData%\Programs\Git\bin\bash.exe"
) do (
    if exist %%p set "BASH=%%p"
)

if "%BASH%"=="" (
    echo Git Bash не найден. Установи Git for Windows или запусти deploy.sh через WSL.
    exit /b 1
)

%BASH% -c "cd '%~dp0' && bash deploy.sh %1"
