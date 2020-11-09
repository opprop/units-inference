package units;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;
import java.lang.annotation.Annotation;
import java.util.Set;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.source.SupportedOptions;
import units.representation.UnitsRepresentationUtils;

@StubFiles({
    "JavaBoxedPrimitives.astub",
    "JavaIOPrintstream.astub",
    "JavaLang.astub",
    "JavaMath.astub",
    "JavaMathTrig.astub",
    "JavaThread.astub",
    "JavaUtil.astub",
    "JavaText.astub",
    "JavaNioFile.astub",
    "JavolutionMathLib.astub",
    "ApacheCommonsExec.astub",
    "DaikonExprPlumelib.astub", // for daikon expr
    "JavaUtilConcurrent.astub", // for ode4j, not yet annotated for hombucha
    //    "ExperimentsJavaAwtGeomAffineTransform.astub", // for imgscalr experiment
    "JavaAWT.astub", // for josm
    "JavaAWTGeom.astub", // for josm, imgscalr
    "ExperimentsSunMiscUnsafe.astub", // for JLargeArrays
})
@SupportedOptions({UnitsChecker.DISABLE_RDU})
public class UnitsChecker extends BaseInferrableChecker {

    /** Command-line option that enables receiver dependent unit. */
    public static final String DISABLE_RDU = "disableRDU";

    @Override
    public void initChecker() {
        super.initChecker();
    }

    @Override
    public UnitsVisitor createVisitor(
            InferenceChecker iChecker, BaseAnnotatedTypeFactory factory, boolean infer) {
        return new UnitsVisitor(this, iChecker, factory, infer);
    }

    @Override
    public UnitsAnnotatedTypeFactory createRealTypeFactory() {
        return new UnitsAnnotatedTypeFactory(this);
    }

    @Override
    public UnitsInferenceAnnotatedTypeFactory createInferenceATF(
            InferenceChecker inferenceChecker,
            InferrableChecker realChecker,
            BaseAnnotatedTypeFactory realTypeFactory,
            SlotManager slotManager,
            ConstraintManager constraintManager) {
        return new UnitsInferenceAnnotatedTypeFactory(
                inferenceChecker, realTypeFactory, realChecker, slotManager, constraintManager);
    }

    @Override
    public boolean isInsertMainModOfLocalVar() {
        return true;
    }

    @Override
    public boolean withCombineConstraints() {
        return false;
    }

    @Override
    public Set<Class<? extends Annotation>> additionalAnnotationsForJaifHeaderInsertion() {
        return UnitsRepresentationUtils.getInstance().surfaceUnitsSet();
    }
}
