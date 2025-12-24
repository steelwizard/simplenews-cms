#/bin/bash

docker-compose down

./mvnw clean package --batch-mode
docker build -t cms .
docker-compose build

docker-compose up -d