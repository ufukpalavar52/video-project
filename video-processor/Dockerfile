FROM eclipse-temurin:23-jdk AS builder

ARG MAVEN_VERSION=3.9.11
ARG MAVEN_HOME=/usr/share/maven

RUN apt-get update && \
    apt-get install -y wget unzip && \
    rm -rf /var/lib/apt/lists/*

RUN wget -q https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip -O /tmp/maven.zip && \
    unzip -q /tmp/maven.zip -d /usr/share && \
    ln -s /usr/share/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    rm /tmp/maven.zip

ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /app

COPY video-processor/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

COPY video-processor/src ./src

RUN mvn clean package -DskipTests

RUN mv target/video-processor-0.0.1-SNAPSHOT.jar /app/app.jar && rm -rf src && rm -rf target

FROM eclipse-temurin:23-jre-alpine AS runner

WORKDIR /app
COPY --from=builder /app/app.jar  ./

ENTRYPOINT ["java", "-jar", "app.jar"]