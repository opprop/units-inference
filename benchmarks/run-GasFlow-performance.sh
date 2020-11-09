#!/bin/bash

echo "This script runs test project GasFlow"

WORKING_DIR=$(cd $(dirname "$0") && pwd)
UI=$WORKING_DIR/..

## Fetch checker-framework-inference
if [ -d $WORKING_DIR/GasFlow ] ; then
    (cd $WORKING_DIR/GasFlow)
else
    echo "Cloning from branch" $BRANCH
    (cd $WORKING_DIR && git clone https://github.com/txiang61-benchmarks/GasFlow.git)
fi

GASFLOW=$WORKING_DIR/GasFlow

# evaluate original changes
echo "compile project original files"
: $(cd $GASFLOW && git checkout -f original_test && git pull && ant clean compile jar)
echo "=== Original performance results ==="
(cd $GASFLOW && java -cp build/classes:lib/JSAP-2.1.jar:lib/jscience.jar:lib/guava-24.1-jre.jar gas.algo.SourceSinkForwardComputation)

# evaluate punits changes
echo "compile project punits changes"
: $(cd $GASFLOW && git checkout -f punit_expr && git pull && cp $UI/build/libs/units-inference.jar $GASFLOW/lib/. && ant clean compile jar)
echo "=== PUnits performance results ==="
(cd $GASFLOW && java -cp build/classes:lib/units-inference.jar:lib/JSAP-2.1.jar:lib/jscience.jar:lib/guava-24.1-jre.jar gas.algo.SourceSinkForwardComputation)

echo "Performance test done."
