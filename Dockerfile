FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SERVER_PORT=8080
ENV SPRING_PROFILE=release
ENV LOG_PATH_PREFIX='/log'

ENTRYPOINT ["sh", "-c", "java -jar -Xms256m -Xmx512m -Xlog:gc*,safepoint:file=$LOG_PATH_PREFIX/gc_%p.log:time,uptime,level,tags:filecount=10,filesize=20M -Dserver.port=$SERVER_PORT -Dspring.profiles.active=$SPRING_PROFILE /app.jar > $LOG_PATH_PREFIX/app.log 2>&1"]