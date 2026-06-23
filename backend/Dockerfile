FROM openjdk:8-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 18080
ENTRYPOINT ["java", "-jar", "app.jar"]
