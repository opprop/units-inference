#!/bin/bash

# Fail the whole script if any command fails
set -e

WORKING_DIR=$(cd $(dirname "$0") && pwd)
export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
export JSR308=$(cd $(dirname "$0")/.. && pwd)

# export SHELLOPTS

#default value is opprop. REPO_SITE may be set to other value for travis test purpose.
export REPO_SITE="${REPO_SITE:-opprop}"

echo "------ Downloading everthing from REPO_SITE: $REPO_SITE ------"

## Fetch checker-framework-inference
if [ -d $JSR308/checker-framework-inference ] ; then
    (cd $JSR308/checker-framework-inference && git pull)
else
    BRANCH=master
    echo "Cloning from branch" $BRANCH
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/checker-framework-inference.git)
fi

## Fetch DLJC
if [ -d $JSR308/do-like-javac ] ; then
    (cd $JSR308/do-like-javac && git pull)
else
    BRANCH=master
    echo "Cloning from branch" $BRANCH
    (cd $JSR308 && git clone -b $BRANCH --depth 1 https://github.com/"$REPO_SITE"/do-like-javac.git)
fi

## Build checker-framework-inference
(cd $JSR308/checker-framework-inference && ./.ci-build-without-test.sh)

## Build units-inference without testing
(cd $JSR308/units-inference && ./gradlew build -x test)
