# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-22-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Run
FROM eclipse-temurin:22.0.1_8-jre-alpine
WORKDIR /app

# Копируем JAR из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Копируем конфигурационные файлы
COPY src/main/resources/application.properties application.properties
COPY src/main/resources/email.properties email.properties
COPY src/main/resources/sms.properties sms.properties
COPY src/main/resources/telegram.properties telegram.properties
COPY src/main/resources/logging.properties logging.properties

# Открываем порт API
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]