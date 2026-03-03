FROM maven:3.8.5-openjdk-17

WORKDIR /app
# Al arrancar, Maven escuchará cambios si tienes DevTools
CMD ["mvn", "spring-boot:run"]