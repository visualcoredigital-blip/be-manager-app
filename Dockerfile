# ETAPA 1: Compilación (Funciona en Render y en tu PC)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Copiamos solo lo necesario para compilar
COPY pom.xml .
COPY src ./src
# Compilamos y generamos el JAR (se guarda en /app/target/)
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (La imagen final que corre la app)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Traemos el JAR de la etapa anterior y lo renombramos a app.jar para simplificar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# El ENTRYPOINT ahora es simple y no depende de carpetas externas
ENTRYPOINT ["java", "-jar", "app.jar"]