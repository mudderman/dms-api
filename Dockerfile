FROM --platform=linux/amd64 adoptopenjdk/openjdk11:alpine-jre
#FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /opt/app
COPY target/dms-api.jar .

ENTRYPOINT ["java", "-jar", "dms-api.jar"]
