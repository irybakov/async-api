#!/usr/bin/env bash
# clean & build sbt project
sbt clean test

# create jar file
sbt dist

# build docker
docker build -t async-api .
docker tag -f async-api:latest irybakov/async-api
docker push irybakov/async-api

