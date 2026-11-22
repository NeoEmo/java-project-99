# syntax=docker/dockerfile:1
FROM gradle:9.9.1-jdk25-alpine AS build

WORKDIR /home/gradle/app

COPY --chown=gradle:gradle gradlew settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle
RUN --mount=type=cache,target=/home/gradle/.gradle/caches,uid=1000,gid=1000 \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,uid=1000,gid=1000 \
    ./gradlew --no-daemon dependencies

COPY --chown=gradle:gradle src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle/caches,uid=1000,gid=1000 \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,uid=1000,gid=1000 \
    ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre-alpine AS runtime

RUN apk add --no-cache openssl \
    && addgroup -S -g 10001 app \
    && adduser -S -D -H -u 10001 -G app app \
    && mkdir -p /run/secrets \
    && chown app:app /run/secrets

WORKDIR /app
COPY --from=build --chown=app:app /home/gradle/app/build/libs/*.jar /app/app.jar
COPY --chown=app:app entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod 0755 /usr/local/bin/entrypoint.sh

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
USER app
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["java", "-jar", "/app/app.jar"]
STOPSIGNAL SIGTERM
