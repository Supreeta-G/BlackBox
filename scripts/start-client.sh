#!/usr/bin/env bash
echo "========================================================"
echo "Starting AI Agent Black Box - JavaFX Desktop Client"
echo "========================================================"
cd "$(dirname "$0")/.."
mvn javafx:run -pl desktop-client
