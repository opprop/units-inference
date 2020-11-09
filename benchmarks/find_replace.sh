#!/bin/bash
# find_replace.sh

# echo "Find and replace in current directory!"
# echo "Existing string?"
# read existing
# echo "Replacement string?"
# read replacement
# echo "Replacing all occurences of $existing with $replacement in files matching $filepattern"

# find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/$existing/$replacement/g"

find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/import si.uom.SI;/import units.UnitsTools;/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.METRE/UnitsTools.m/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.RADIAN/UnitsTools.rad/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.SECOND/UnitsTools.s/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.KILOMETER/UnitsTools.km/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.DAY/UnitsTools.day/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/SI.YEAR/UnitsTools.year/g"

find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/import si.uom.NonSI;/import units.UnitsTools;/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/NonSI.DEGREE_ANGLE/UnitsTools.deg/g"

find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/import javax.measure.Unit;//g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<?>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<Length>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<Angle>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<Time>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<T>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<Q>/int/g"
find . -type f -name "*.java" -print0 | xargs -0 sed -i '' -e "s/Unit<Dimensionless>/int/g"
