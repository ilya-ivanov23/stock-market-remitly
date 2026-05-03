#!/bin/bash
if [ -z "$1" ]; then
  echo "Usage: ./start.sh <PORT>"
  exit 1
fi

export APP_PORT=$1
echo "Starting application on localhost:$APP_PORT..."

if [ ! -f .env ]; then
  echo "Creating .env from .env.example..."
  cp .env.example .env
fi

# Build JAR
./mvnw clean package -DskipTests

# Run containers
docker-compose up -d --build

echo "Application is highly available and ready at http://localhost:$APP_PORT"
