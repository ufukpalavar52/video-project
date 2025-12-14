FROM maven:3.9.9-eclipse-temurin-23

WORKDIR /app

COPY video-processor/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

CMD ["mvn", "spring-boot:run"]