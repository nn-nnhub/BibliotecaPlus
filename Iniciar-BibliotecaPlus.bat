@echo off
title BibliotecaPlus
cd /d "%~dp0"

echo Iniciando BibliotecaPlus...
start "BibliotecaPlus - Servidor" cmd /k "C:\Users\USER\Tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run"

echo Esperando a que el servidor este disponible...
timeout /t 8 /nobreak >nul
start "" "http://localhost:8080"

echo La aplicacion se abrira en el navegador.
