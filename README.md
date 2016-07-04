# async-api

[![Build Status](https://snap-ci.com/irybakov/async-api/branch/master/build_image)](https://snap-ci.com/irybakov/async-api/branch/master)
[![Coverage Status](https://coveralls.io/repos/github/irybakov/async-api/badge.svg?branch=master)](https://coveralls.io/github/irybakov/async-api?branch=master)



## overview
Sample of Async API MicroService.

Part of Infrastracture prototype.

## Quick launch

    docker pull irybakov/async-api
    
    docker run -d irybakov/async-api
    

## Build

    Build and push to your own docker hub

### sbt build 
    sbt clean compile dist
    
### docker build
    
    docker build -t async-api
    docker tag async-stub irybakov/async-api
    
