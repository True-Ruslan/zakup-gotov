FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace/apps/api

COPY apps/api/.mvn .mvn
COPY apps/api/mvnw apps/api/pom.xml ./
RUN ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY apps/api/src src
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:25-jre-noble AS runtime

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/apps/api/target/zakup-gotov-api-*.jar /app/app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=10 \
  CMD curl --fail --silent http://127.0.0.1:8080/actuator/health/readiness >/dev/null || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
