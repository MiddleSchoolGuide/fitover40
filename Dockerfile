FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

ENV FITOVER40_BACKEND_ONLY=true

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY local-auth-server local-auth-server

RUN chmod +x ./gradlew && ./gradlew :local-auth-server:installDist --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV FITOVER40_BACKEND_ONLY=true

COPY --from=build /app/local-auth-server/build/install/local-auth-server ./local-auth-server

EXPOSE 8080

CMD ["./local-auth-server/bin/local-auth-server"]
