#!/bin/bash

echo "This script runs type check on project Daikon"

if ! [ -n "$1" ]; then
    echo "Please specify which daikon branch you want to run on."
    exit 1
fi

WORKING_DIR=$(cd $(dirname "$0") && pwd)

## Fetch daikon projects
if [ -d $WORKING_DIR/daikon ] ; then
    (cd $WORKING_DIR/daikon)
else
    (cd $WORKING_DIR && git clone https://github.com/txiang61-benchmarks/daikon.git)
fi

DAIKON=$WORKING_DIR/daikon

echo "Compile PUnits"
: $(update-alternatives --config java <<< 2)
(cd $PUNITS && ./gradlew assemble)

if [ -n "$1" ] && [ $1 = "master" ]; then
    echo "Checking out branch master from $(DAIKON)"
    : $(cd $DAIKON && git checkout master && git pull)
    echo "Run type check on Daikon unannotated version"
    shift
elif [ -n "$1" ] && [ $1 = "unit-error" ]; then
    echo "Checking out branch unit-error from $(DAIKON)"
    : $(cd $DAIKON && git checkout unit-error && git pull)
    echo "Run type check on Daikon fully annotated with bug inserted"
    shift
elif [ -n "$1" ] && [ $1 = "error-fixed" ]; then
    echo "Checking out branch error-fixed from $(DAIKON)"
    : $(cd $DAIKON && git checkout error-fixed && git pull)
    echo "Run type check on Daikon fully annotated with bug corrected"
    shift
else
    echo "Unknown branch"
    exit 1
fi

(cd $DAIKON/java && time make check-units)
