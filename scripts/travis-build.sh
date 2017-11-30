#!/bin/bash

SBT="sbt ++${TRAVIS_SCALA_VERSION}"
COVERAGE="$SBT clean coverage test coverageReport"
NO_COVERAGE="$SBT clean test"

if [[ ${TRAVIS_SCALA_VERSION} == 2.12.* ]]; then
    ${COVERAGE} && codecov
else
    ${NO_COVERAGE}
fi
