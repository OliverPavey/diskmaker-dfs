#!/bin/bash

SCRIPTFOLDER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

if [ 0 -eq $(docker volume ls | grep maven-repo | wc -l) ]; then
    docker volume create --name maven-repo
fi

docker run --rm \
    --volume maven-repo:/root/.m2 \
    --volume $SCRIPTFOLDER:/diskmaker \
    -w /diskmaker \
    maven:3-jdk-8 \
    mvn clean package
