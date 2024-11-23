# Etap 1: Budowa aplikacji React
FROM node:21.7.1 AS build-react

WORKDIR /app/react

# Kopiujemy pliki package.json i package-lock.json
COPY src/main/resources/react/package*.json ./

# Instalujemy zależności npm
RUN npm install

# Kopiujemy wszystkie pliki React
COPY src/main/resources/react ./

# Budujemy aplikację React
RUN npm run webpackbuild


# Etap 2: Budowa aplikacji Ktor
FROM eclipse-temurin:21 AS build-ktor

WORKDIR /app

# Kopiujemy pliki projektu Ktor
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

# Ustawiamy prawa wykonania dla skryptu Gradle Wrapper
RUN chmod +x gradlew

# Budujemy aplikację Ktor (FatJar)
RUN ./gradlew buildFatJar


# Etap 3: Finalny obraz
FROM eclipse-temurin:21

WORKDIR /app

# Kopiujemy zbudowaną aplikację React z poprzedniego etapu
COPY --from=build-react /app/react/build ./src/main/resources/react/build

# Kopiujemy gotowy JAR z poprzedniego etapu
COPY --from=build-ktor /app/build/libs/com.pollub.awpfo-all.jar ./com.pollub.awpfo-all.jar

# Kopiujemy certyfikat SSL
COPY keystore.jks ./keystore.jks

# Ustawiamy zmienne środowiskowe dla certyfikatu SSL
ENV JAVA_OPTS="-Djavax.net.ssl.keyStore=/app/keystore.jks -Djavax.net.ssl.keyStorePassword=qwerty"

# Otwieramy port HTTPS
EXPOSE 443

# Uruchamiamy aplikację Ktor
CMD ["java", "-jar", "/app/com.pollub.awpfo-all.jar"]
