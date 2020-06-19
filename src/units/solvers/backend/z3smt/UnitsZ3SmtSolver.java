package units.solvers.backend.z3smt;

import java.util.Collection;

import com.microsoft.z3.Expr;

import checkers.inference.InferenceMain;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;
import units.representation.TypecheckUnit;
import units.solvers.backend.z3smt.representation.Z3InferenceUnit;

public class UnitsZ3SmtSolver extends Z3SmtSolver<Z3InferenceUnit, TypecheckUnit> {

    public UnitsZ3SmtSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Z3SmtFormatTranslator<Z3InferenceUnit, TypecheckUnit> z3SmtFormatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, z3SmtFormatTranslator, lattice);
    }

    @Override
    protected void encodeSoftSubtypeConstraint(SubtypeConstraint stc) {
        Constraint eqc =
                InferenceMain.getInstance()
                        .getConstraintManager()
                        .createEqualityConstraint(stc.getSubtype(), stc.getSupertype());

        Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();

        if (!simplifiedEQC.isTrue()) {
            addSoftConstraint(simplifiedEQC, 1);
        }
    }

    @Override
    protected void encodeSoftComparableConstraint(ComparableConstraint cc) {
    	Constraint eqc =
                InferenceMain.getInstance()
                        .getConstraintManager()
                        .createEqualityConstraint(cc.getFirst(), cc.getSecond());

        Expr simplifiedEQC = eqc.serialize(formatTranslator).simplify();

        if (!simplifiedEQC.isTrue()) {
        	addSoftConstraint(simplifiedEQC, 1);
        }
    }
    
}
