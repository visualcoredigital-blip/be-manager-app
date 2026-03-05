FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# CAMBIO AQUÍ: Apuntamos a la ruta real dentro del volumen
ENTRYPOINT ["java", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]