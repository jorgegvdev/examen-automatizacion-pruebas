# Etapa 1: build del jar ejecutable
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

# Etapa 2: imagen de runtime, liviana (sin Maven ni JDK completo)
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/taller-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
