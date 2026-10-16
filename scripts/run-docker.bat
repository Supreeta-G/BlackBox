@echo off
echo ========================================================
echo Starting Docker Infrastructure (PostgreSQL & Kafka)
echo ========================================================
cd /d "%~dp0\.."
docker compose up -d postgres kafka
echo.
echo Checking service status:
docker compose ps
pause
