FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy only what is necessary for a reproducible Maven build
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the jar produced by the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
