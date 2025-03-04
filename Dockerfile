FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SERVER_PORT=8080
ENV SPRING_PROFILE=release
ENV LOG_PATH_PREFIX='/log'

ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=$SERVER_PORT -Dspring.profiles.active=$SPRING_PROFILE /app.jar > $LOG_PATH_PREFIX/app.log 2>&1"]