FROM adoptopenjdk/openjdk16:alpine-slim as build
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src
RUN ./mvnw package -DskipTests
FROM adoptopenjdk/openjdk16:alpine-jre
RUN apk add curl
COPY --from=build target/backend-0.0.1-SNAPSHOT.jar backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "backend.jar"]

