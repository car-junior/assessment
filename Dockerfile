FROM amazoncorretto:17-alpine

COPY ./target/assessment-1.0.0.jar /app/app.jar

# Exponha a porta 8080
EXPOSE 8080

# Comando para executar o aplicativo quando o contêiner iniciar
CMD ["java", "-jar", "/app/app.jar"]
