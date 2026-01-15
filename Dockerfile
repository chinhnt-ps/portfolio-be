# Multi-stage build để tối ưu image size
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml trước để cache dependencies
COPY pom.xml .
COPY .mvn .mvn

# Download dependencies (sẽ được cache nếu pom.xml không đổi)
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR từ build stage
COPY --from=build /app/target/portfolio-be-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
