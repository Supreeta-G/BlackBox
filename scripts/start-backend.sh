#!/usr/bin/env bash
echo "========================================================"
echo "Starting AI Agent Black Box - Spring Boot Backend"
echo "========================================================"
cd "$(dirname "$0")/.."
mvn spring-boot:run -pl backend
