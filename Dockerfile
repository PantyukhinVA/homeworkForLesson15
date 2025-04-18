# Базовый образ с Java и Maven
FROM maven:3.8.6-eclipse-temurin-17 as builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
RUN mvn package

# Финальный образ
FROM eclipse-temurin:17-jre

# Установка Chrome и зависимостей
RUN apt-get update && \
    apt-get install -y wget gnupg2 && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable xvfb libgbm-dev && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/chrome-test.jar /app/
COPY --from=builder /app/target/dependency /app/libs/

CMD xvfb-run -a java -jar /app/chrome-test.jar