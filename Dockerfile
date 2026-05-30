# ETAPA 1: Compilación (Optimizado para evitar bloqueos de memoria en la nube)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiamos solo lo necesario para compilar
COPY pom.xml .
COPY src ./src

# Compilamos usando un solo hilo de compilación (-Dmaven.compiler.fork=false) 
# Esto evita que el build se congele por falta de RAM en la instancia de Render
RUN mvn clean package -DskipTests -Dmaven.compiler.fork=false

# ETAPA 2: Ejecución (La imagen final que corre la app)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Traemos el JAR de la etapa anterior y lo renombramos a app.jar para simplificar
COPY --from=build /app/target/*.jar app.jar

# Nota: Render ignorará este EXPOSE y usará el puerto asignado en la variable PORT (10000)
# Tu application.properties se encargará de enlazarlo automáticamente.
EXPOSE 8080

# El ENTRYPOINT ahora es simple y ejecutará el JAR leyendo las variables del entorno
ENTRYPOINT ["java", "-jar", "app.jar"]