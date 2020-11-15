FROM gradle:6.7.0-jdk11 as builder

WORKDIR /tmp/app

COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM openjdk:11-jre-slim-stretch

COPY --from=builder /tmp/app/build/libs/service-0.0.1-SNAPSHOT.jar /opt/app.jar
COPY dict /dict

ENV DICTIONARY_DIR=/dict

CMD ["java","-jar","/opt/app.jar"]