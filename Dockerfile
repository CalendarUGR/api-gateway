FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8090
COPY ./target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar
COPY .env .env

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "api-gateway.jar"]