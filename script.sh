#!/usr/bin/zsh
clear

docker compose down --rmi all

./mvnw clean package -DskipTests

docker compose up --build