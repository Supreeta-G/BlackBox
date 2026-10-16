@echo off
echo ========================================================
echo Starting AI Agent Black Box - Spring Boot Backend
echo ========================================================
cd /d "%~dp0\.."
mvn spring-boot:run -pl backend
pause
