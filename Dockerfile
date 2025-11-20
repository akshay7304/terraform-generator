
# ==========================
# 1) Build Stage - uses Java 17 + Maven
# ==========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy everything into the container
COPY . .

# Build the Spring Boot application JAR
RUN mvn clean package -DskipTests

# ==========================
# 2) Run Stage - lightweight Java 17 JRE
# ==========================
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy only the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose app port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
