@echo off
title BibliotecaPlus
cd /d "%~dp0"

set "PROJECT_DIR=%~dp0"
set "JAVA_HOME="
set "MAVEN_HOME="

for /d %%D in ("%PROJECT_DIR%.tools\jdk-*") do if exist "%%~D\bin\java.exe" set "JAVA_HOME=%%~D"
for /d %%D in ("%PROJECT_DIR%.tools\apache-maven-*") do if exist "%%~D\bin\mvn.cmd" set "MAVEN_HOME=%%~D"

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%JAVA_EXE%" (
    echo Java portable no encontrado. Preparando herramientas locales...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%bootstrap-portable.ps1"
    if errorlevel 1 (
        echo No se pudo preparar Java portable.
        pause
        exit /b 1
    )
    set "JAVA_HOME=%PROJECT_DIR%.tools\jdk-17"
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

if not exist "%MAVEN_CMD%" (
    echo Maven portable no encontrado. Preparando herramientas locales...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%PROJECT_DIR%bootstrap-portable.ps1"
    if errorlevel 1 (
        echo No se pudo preparar Maven portable.
        pause
        exit /b 1
    )
    set "MAVEN_HOME=%PROJECT_DIR%.tools\apache-maven-3.9.9"
    set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
)

echo Iniciando BibliotecaPlus con Java y Maven portables...
start "BibliotecaPlus - Servidor" cmd /k "set JAVA_HOME=%JAVA_HOME%&& set MAVEN_HOME=%MAVEN_HOME%&& \"%MAVEN_CMD%\" spring-boot:run"

echo Esperando a que el servidor este disponible...
timeout /t 8 /nobreak >nul
start "" "http://localhost:8080"

echo La aplicacion se abrira en el navegador.
