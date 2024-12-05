FROM openjdk:21
WORKDIR /app
COPY target/*.jar ./
EXPOSE 3888
CMD ["java","-Duser.timezone=Asia/Jakarta", "-jar", "ticket-0.0.1.jar"]