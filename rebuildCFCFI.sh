#!/bin/bash

: $(update-alternatives --config java <<< 2)

echo "Rebuilding CF"
(cd ../checker-framework && git pull && ./gradlew assemble)

echo "Rebuilding CFI"
(cd ../checker-framework-inference && git pull && ./gradlew dependenciesJar dist)
