FROM  openjdk:16 as build
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src
RUN ./mvnw package -DskipTests
FROM  openjdk:16
COPY --from=build target/backend-0.0.1-SNAPSHOT.jar backend.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "backend.jar"]

