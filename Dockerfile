FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SERVER_PORT=8080
ENV SPRING_PROFILE=release
ENV LOG_PATH_PREFIX=/log

# GC 로그
#-Xlog:gc*,safepoint:file=$LOG_PATH_PREFIX/gc/gc_%p.log:time,uptime,level,tags:filecount=10,filesize=20M \

# JMX(VisualVM)
#-Dcom.sun.management.jmxremote=true \
#-Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 \
#-Dcom.sun.management.jmxremote.local.only=false \
#-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
#-Djava.rmi.server.hostname=127.0.0.1 \
ENTRYPOINT ["java",
"-Xms256m", "-Xmx512m",
"-Xlog:gc*,safepoint:file=$LOG_PATH_PREFIX/gc/gc_%p.log:time,uptime,level,tags:filecount=10,filesize=20M",
"-Dserver.port=$SERVER_PORT", "-Dspring.profiles.active=$SPRING_PROFILE",
"-jar", "/app.jar", ">", "$LOG_PATH_PREFIX/app.log", "2>&1"]