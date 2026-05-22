@echo off
cd /d "%~dp0"
echo Foro Sanos y Salvos (MySQL XAMPP, puerto 8099)
echo Asegurate de tener MySQL activo en XAMPP.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-xampp.ps1" -Service forum
pause
