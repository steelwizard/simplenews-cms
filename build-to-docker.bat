@echo off

docker-compose down

call mvnw clean package --batch-mode
docker build -t cms .
docker-compose build

rem docker-compose up -d
docker-compose up
