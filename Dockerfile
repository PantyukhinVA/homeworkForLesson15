FROM maven:3.8.6-eclipse-temurin-17

# Установка Chrome и зависимостей
RUN apt-get update && \
    apt-get install -y wget gnupg2 && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable xvfb libgbm-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

CMD xvfb-run -a mvn test