#!/bin/bash
sbt assembly
docker-compose build
docker-compose rm
docker-compose up