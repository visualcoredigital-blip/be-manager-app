# ETAPA 1: Compilación (Build)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Generamos el JAR. Esto NO usa el plugin de ejecución, solo el de empaquetado.
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copiamos el archivo JAR exacto que me confirmaste
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar
# Exponemos el puerto del Backend-Manager
EXPOSE 8000
# Ejecutamos con Java puro, evitando Maven por completo en el inicio
ENTRYPOINT ["java", "-jar", "app.jar"]