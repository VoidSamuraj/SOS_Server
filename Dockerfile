# Użyj oficjalnego obrazu JDK jako bazowego
FROM eclipse-temurin:21

# Ustaw katalog roboczy
WORKDIR /app

# Skopiuj pliki Gradle
COPY build.gradle.kts settings.gradle.kts gradlew gradle.properties ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew

# Zbuduj aplikację Ktor
RUN ./gradlew buildFatJar


# Użyj Node.js do budowy aplikacji React
FROM node:21.7.1 as build-react

# Ustaw katalog roboczy dla aplikacji React
WORKDIR src/main/resources/react

# Skopiuj pliki package.json i package-lock.json
COPY src/main/resources/react/package*.json src/main/resources/react ./

# Zainstaluj zależności npm
RUN npm install

# Zbuduj aplikację React
RUN npm run webpackbuild

# Użyj obrazu JDK do uruchomienia aplikacji Ktor
FROM eclipse-temurin:21

# Ustaw katalog roboczy
WORKDIR /app

# Skopiuj zbudowaną aplikację React z katalogu build
COPY --from=build-react src/main/resources/react/build ./src/main/resources/react/build

# Skopiuj pliki projektu Ktor do kontenera
COPY build/libs/com.pollub.awpfo-all.jar /app/com.pollub.awpfo-all.jar

# Skopiuj certyfikat SSL do kontenera
COPY keystore.jks /app/keystore.jks

# Ustaw zmienną środowiskową dla JAVA_OPTS
ENV JAVA_OPTS="-Djavax.net.ssl.keyStore=/app/keystore.jks -Djavax.net.ssl.keyStorePassword=qwerty"

# Otwórz port 8443
EXPOSE 8443

# Uruchom aplikację Ktor
CMD ["java", "-jar", "/app/com.pollub.awpfo-all.jar"]
