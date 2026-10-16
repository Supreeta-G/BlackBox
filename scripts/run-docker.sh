#!/usr/bin/env bash
echo "========================================================"
echo "Starting Docker Infrastructure (PostgreSQL & Kafka)"
echo "========================================================"
cd "$(dirname "$0")/.."
docker compose up -d postgres kafka
docker compose ps
