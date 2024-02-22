FROM --platform=amd64 openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/Stoury-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENV DB_USERNAME=default_username
ENV DB_PASSWORD=default_password
ENV JOB_NAME=default_jobname
ENV GEOCODE_APIKEY=default_apikey
ENV ORIGIN=default_origin
ENV TOKEN_SECRET=default_token_secret
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]