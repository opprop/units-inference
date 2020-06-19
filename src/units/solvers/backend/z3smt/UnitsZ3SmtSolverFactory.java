package units.solvers.backend.z3smt;

import java.util.Collection;

import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolverFactory;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;
import units.representation.TypecheckUnit;
import units.solvers.backend.z3smt.representation.Z3InferenceUnit;

public class UnitsZ3SmtSolverFactory extends Z3SmtSolverFactory<Z3InferenceUnit, TypecheckUnit> {

    @Override
    protected Z3SmtFormatTranslator<Z3InferenceUnit, TypecheckUnit> createFormatTranslator(
            Lattice lattice) {
        return new UnitsZ3SmtFormatTranslator(lattice);
    }
    
    @Override
    public Solver<?> createSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Lattice lattice) {
        Z3SmtFormatTranslator<Z3InferenceUnit, TypecheckUnit> formatTranslator =
                createFormatTranslator(lattice);
        return new UnitsZ3SmtSolver(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }
}
