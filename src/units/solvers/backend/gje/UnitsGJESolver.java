package units.solvers.backend.gje;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.serialization.ToStringSerializer;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverEnvironment;
import checkers.inference.solver.util.Statistics;

import org.checkerframework.javacutil.BugInCF;

import units.solvers.backend.gje.representation.GJEEquationSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;

// GaussJordanElimination solver
public class UnitsGJESolver extends Solver<UnitsGJEFormatTranslator> {

    protected final Logger logger = Logger.getLogger(UnitsGJESolver.class.getName());

    // file is written at projectRootFolder/gjeConstraints_<dimension>.gje
    protected static final Path pathToProject = Paths.get(System.getProperty("user.dir"));
    protected static final Path constraintsFilePrefix =
            Paths.get(pathToProject.toString(), "gjeConstraints");
    protected static final String constraintsFileExtension = ".gje";

    // timing statistics variables
    protected long serializationStart;
    protected long serializationEnd;
    protected long solvingStart;
    protected long solvingEnd;

    // number of GJE variables
    protected int numOfGJEVariables;

    public UnitsGJESolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            UnitsGJEFormatTranslator formatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    // Main entry point
    @Override
    public Map<Integer, AnnotationMirror> solve() {
        Map<Integer, AnnotationMirror> result;

        serializationStart = System.currentTimeMillis();
        numOfGJEVariables = formatTranslator.assignGJEVarIDs(constraints);
        encodeAllConstraints();
        serializationEnd = System.currentTimeMillis();

        solvingStart = System.currentTimeMillis();
        List<String> results = runSolver();
        solvingEnd = System.currentTimeMillis();

        long serializationTime = serializationEnd - serializationStart;
        long solvingTime = solvingEnd - solvingStart;
        Statistics.addOrIncrementEntry("gje_serialization_time(ms)", serializationTime);
        Statistics.addOrIncrementEntry("gje_solving_time(ms)", solvingTime);

        if (results != null) {
            result =
                    formatTranslator.decodeSolution(
                            results, solverEnvironment.processingEnvironment);
        } else {
            System.err.println("\n\n!!! The set of constraints is unsatisfiable! !!!");
            result = null;
        }

        return result;
    }

    @Override
    protected void encodeAllConstraints() {

        final ToStringSerializer toStringSerializer = new ToStringSerializer(false);

        GJEEquationSet totalEquationSet = new GJEEquationSet();

        for (Constraint constraint : constraints) {

            // System.err.println("Serializing " +
            // constraint.serialize(toStringSerializer));

            GJEEquationSet serializedConstraint = constraint.serialize(formatTranslator);

            if (serializedConstraint == null) {
                System.err.println(
                        "Unsupported constraint detected! Constraint type: "
                                + constraint.getClass().getSimpleName());
                continue;
            } else if (serializedConstraint.isContradiction()) {
                // TODO: proper error reporter abort
                throw new BugInCF(
                        "the constraint "
                                + constraint.serialize(toStringSerializer)
                                + " is contradictory");
            } else if (!serializedConstraint.isEmpty()) {
                totalEquationSet.union(serializedConstraint);
            }

            // System.err.println(serializedConstraint.toString());
        }

        // System.err.println("Total equation set:");
        // System.err.println(totalEquationSet);

        // serialize into files
        writeGJEFiles(totalEquationSet.getEquationSet());
    }

    private void writeGJEFiles(Map<String, Set<String>> eqSet) {
        for (Entry<String, Set<String>> entry : eqSet.entrySet()) {
            String dimension = entry.getKey();
            Set<String> equations = entry.getValue();

            String fileName =
                    constraintsFilePrefix.toString() + "_" + dimension + constraintsFileExtension;

            File outFile = new File(fileName);

            FileUtils.writeFile(outFile, generateGJEFileContent(equations));

            System.err.println(
                    "GJE eqs for "
                            + dimension
                            + " have been written to: "
                            + outFile.getAbsolutePath()
                            + System.lineSeparator());
        }
    }

    private String generateGJEFileContent(Set<String> equations) {
        StringBuffer sb = new StringBuffer();
        // # of variables
        sb.append(numOfGJEVariables);
        sb.append(System.lineSeparator());
        // # of rows
        sb.append(equations.size());
        sb.append(System.lineSeparator());
        // sort and write each equation out
        sb.append(String.join(System.lineSeparator(), new TreeSet<>(equations)));
        return sb.toString();
    }

    private List<String> runSolver() {
        // TODO
        return null;
    }

    // outputs
    private void parseStdOut(BufferedReader stdOut, List<String> results) {
        String line = "";

        try {
            while ((line = stdOut.readLine()) != null) {}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Constraint> explainUnsatisfiable() {
        // TODO
        List<Constraint> unsatConstraints = new ArrayList<>();
        return unsatConstraints;
    }
}
