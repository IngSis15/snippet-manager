FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

COPY src src

RUN ./gradlew build --no-daemon


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

COPY ./newrelic/newrelic.jar /app/newrelic/newrelic.jar
COPY ./newrelic/newrelic.yml /app/newrelic/newrelic.yml

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:/app/newrelic/newrelic.jar","-jar","-Dspring.profiles.active=production","app.jar"]