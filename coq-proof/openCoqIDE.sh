#!/bin/bash

# Opens the definitions in their dependent ordering, where
# later file names in the sequence depend on the earlier ones.
coqide TacticalLemmas.v \
  MapsDefs.v \
  UnitsDefs.v \
  LabeledLiterals.v \
  IDDefs.v \
  GammaDefs.v \
  HeapDefs.v \
  SubtypingDefs.v \
  GammaHeapCorrespondence.v \
  UnitsArithmetics.v \
  FieldDefs.v \
  ExpressionDefs.v \
  StatementDefs.v \
  ProgramDefs.v \
  &