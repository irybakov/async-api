#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image OpenJDK
FROM java:8

MAINTAINER Igor Rybakov <igor@rybakov.kz>

ENV APP_NAME parking-api-1.0.zip
ENV APP_DIR parking-api-1.0
ENV JAVA_OPTS -Xms128M -Xmx512M -Xss1M -XX:+CMSClassUnloadingEnabled
ENV RUN_SCRIPT parking-api
#ENV LOG_DIR /eps/logs/store_api
#ENV LOG_ARCHIVE_DIR /eps/logs/archive/system/store_api

# logs
RUN mkdir -p /root/config/
#    && mkdir -p $LOG_DIR \
#    && mkdir -p $LOG_ARCHIVE_DIR

#COPY ./src/main/resources/*logback.xml /root/config/
COPY ./src/main/resources/*.conf /root/config/

WORKDIR /root
COPY ./target/universal/$APP_NAME /root/
RUN unzip -q $APP_NAME
WORKDIR /root/$APP_DIR/bin
CMD chmod +x $RUN_SCRIPT
EXPOSE 8082
#CMD ./$RUN_SCRIPT -Dconfig.resource=/${config}.conf
CMD ./$RUN_SCRIPT -Dconfig.resource=/dev.conf
