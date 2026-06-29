FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package
RUN JAR_PATH="$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*' | head -n 1)" \
    && cp "$JAR_PATH" app.jar


FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS=""

RUN useradd --system --uid 10001 --gid root appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:root /app

COPY --from=build /app/app.jar /app/app.jar

USER appuser

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
