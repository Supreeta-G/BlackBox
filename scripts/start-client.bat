@echo off
echo ========================================================
echo Starting AI Agent Black Box - JavaFX Desktop Client
echo ========================================================
cd /d "%~dp0\.."
mvn javafx:run -pl desktop-client
pause
