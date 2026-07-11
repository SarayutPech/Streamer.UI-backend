# Multi-stage build so the final image only ships a JRE + the built jar,
# not the whole Maven toolchain. Used for deploying to Render (or anywhere
# else that can build from a Dockerfile).

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Cache dependency resolution as its own layer.
RUN mvn -q -e -B dependency:go-offline
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

# Render sets $PORT at runtime; application-prod.yml reads it via ${PORT:8080}.
# Activate the prod + mysql profiles with the SPRING_PROFILES_ACTIVE env var
# in the Render dashboard (not baked in here, so this image still runs fine
# locally with `docker run` for a quick sanity check against JSON storage).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
