# ---------- Stage 1: build the JAR ----------
# Full JDK + Maven, used only to compile. None of this ends up in the final image.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Tests are skipped here: the DAO tests need Docker themselves (Testcontainers),
# and they already ran on your machine.
RUN mvn -B -q -Dmaven.test.skip=true package

# ---------- Stage 2: run it ----------
# Slim Linux (Ubuntu) image with only a Java runtime.
FROM eclipse-temurin:17-jre

# Create an unprivileged system user so the app doesn't run as root
RUN groupadd --system app && useradd --system --gid app --create-home app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/library-app.jar app.jar

USER app

# DB_URL, DB_USER and DB_PASSWORD are supplied at run time (see docker-compose.yml)
ENTRYPOINT ["java", "-jar", "app.jar"]