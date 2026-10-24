FROM eclipse-temurin:25-jre
ARG JAR_FILE=/target/cms*.jar

RUN useradd -u 1000 cms
USER cms

COPY ${JAR_FILE} /opt/cms.jar
ENTRYPOINT ["java","-jar","/opt/cms.jar"]
EXPOSE 8080
