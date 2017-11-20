#!/bin/bash

docker build -t beanpuree .
docker run --name beanpuree_test beanpuree sbt +coverage +test +coverageReport
docker cp beanpuree_test:/usr/src/app/target ./target
docker stop beanpuree_test
docker rm beanpuree_test
