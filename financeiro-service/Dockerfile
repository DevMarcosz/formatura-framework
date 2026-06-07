# Build multi-stage: compila o reator Maven (framework-core + serviço) e roda só o JAR.
# Contexto de build = raiz do projeto (definido no docker-compose.yml).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -q -pl financeiro-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/financeiro-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
