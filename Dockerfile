FROM openjdk:22-jdk-slim
ARG JAR_FILE=/target/cms*.jar
COPY ${JAR_FILE} /opt/cms.jar
ENTRYPOINT ["java","-jar","/opt/cms.jar"]
EXPOSE 8080
