package units;

import checkers.inference.BaseInferenceRealTypeFactory;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree.Kind;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.ViewpointAdapter;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.DefaultForTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotationFormatter;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.framework.util.defaults.QualifierDefaults;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.UserError;

import units.qual.BaseUnit;
import units.qual.PolyUnit;
import units.qual.RDU;
import units.qual.UnitsAlias;
import units.qual.UnitsRep;
import units.representation.UnitsRepresentationUtils;
import units.util.UnitsTypecheckUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

public class UnitsAnnotatedTypeFactory extends BaseInferenceRealTypeFactory {
    // static reference to the singleton instance
    protected static UnitsRepresentationUtils unitsRepUtils;

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        unitsRepUtils = UnitsRepresentationUtils.getInstance(processingEnv, elements);
        postInit();
    }

    @Override
    protected AnnotationClassLoader createAnnotationClassLoader() {
        // Use the Units Annotated Type Loader instead of the default one
        return new UnitsAnnotationClassLoader(checker);
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        // get all the loaded annotations
        Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();
        qualSet.addAll(getBundledTypeQualifiers());

        // // load all the external units
        // loadAllExternalUnits();
        //
        // // copy all loaded external Units to qual set
        // qualSet.addAll(externalQualsMap.values());

        // create internal use annotation mirrors using the base units that have been initialized.
        // must be called here as other methods called within ATF.postInit() requires the annotation
        // mirrors
        unitsRepUtils.postInit();
        // and it should already have some base units
        if (unitsRepUtils.baseUnits().isEmpty()) {
            throw new UserError("Must supply at least 1 base unit to use Units Checker");
        }

        return qualSet;
    }

    @Override
    public AnnotationMirror canonicalAnnotation(AnnotationMirror anno) {
        // check to see if it is an internal units annotation
        if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            // fill in missing base units
            return anno; // unitsRepUtils.fillMissingBaseUnits(anno);
        }

        // check to see if it's a surface annotation such as @m or @UnknownUnits
        for (AnnotationMirror metaAnno :
                anno.getAnnotationType().asElement().getAnnotationMirrors()) {

            // if it has a UnitsAlias or IsBaseUnit meta-annotation, then it must have been prebuilt
            // return the prebuilt internal annotation
            if (AnnotationUtils.areSameByClass(metaAnno, UnitsAlias.class)
                    || AnnotationUtils.areSameByClass(metaAnno, BaseUnit.class)) {

                // System.err.println(" returning prebuilt alias for " + anno.toString());

                return unitsRepUtils.getInternalAliasUnit(anno);
            }
        }

        return super.canonicalAnnotation(anno);
    }

    // Make sure only @UnitsRep annotations with all base units defined are considered supported
    // any @UnitsRep annotations without all base units should go through aliasing to have the
    // base units filled in.
    @Override
    public boolean isSupportedQualifier(AnnotationMirror anno) {
        /*
         * getQualifierHierarchy().getTypeQualifiers() contains PolyUnit, and the AMs of
         * Top and Bottom. We need to check all other instances of @UnitsRep AMs that are
         * supported qualifiers here.
         */
        if (anno == null) {
            return false;
        }
        if (AnnotationUtils.areSameByClass(anno, UnitsRep.class)) {
            return true;
        }
        if (AnnotationUtils.areSameByClass(anno, PolyUnit.class)) {
            return true;
        }
        if (AnnotationUtils.areSameByClass(anno, RDU.class)) {
            return true;
        }
        // Anno is PolyUnit
        return false;
    }

    // Programmatically set the qualifier defaults
    @Override
    protected void addCheckedCodeDefaults(QualifierDefaults defs) {
        // set DIMENSIONLESS as the default qualifier in hierarchy
        defs.addCheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.OTHERWISE);
        // defaults for upper bounds is DIMENSIONLESS, individual bounds can be manually set to
        // UnknownUnits if they want to use units
        defs.addCheckedCodeDefault(unitsRepUtils.DIMENSIONLESS, TypeUseLocation.UPPER_BOUND);
        // defs.addCheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.IMPLICIT_UPPER_BOUND);
        // defs.addCheckedCodeDefault(
        //        unitsRepUtils.DIMENSIONLESS, TypeUseLocation.EXPLICIT_UPPER_BOUND);
        // defaults for lower bounds is BOTTOM, individual bounds can be manually set
        defs.addCheckedCodeDefault(unitsRepUtils.BOTTOM, TypeUseLocation.LOWER_BOUND);
        // exceptions are always dimensionless
        defs.addCheckedCodeDefault(
                unitsRepUtils.DIMENSIONLESS, TypeUseLocation.EXCEPTION_PARAMETER);
        // set TOP as the default qualifier for local variables, for dataflow refinement
        defs.addCheckedCodeDefault(unitsRepUtils.TOP, TypeUseLocation.LOCAL_VARIABLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public QualifierHierarchy createQualifierHierarchy() {
        return MultiGraphQualifierHierarchy.createMultiGraphQualifierHierarchy(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public QualifierHierarchy createQualifierHierarchyWithMultiGraphFactory(
            MultiGraphFactory factory) {
        return new UnitsQualifierHierarchy(factory);
    }

    private final class UnitsQualifierHierarchy extends GraphQualifierHierarchy {
        public UnitsQualifierHierarchy(MultiGraphFactory mgf) {
            super(mgf, unitsRepUtils.BOTTOM);
        }

        /** Programmatically set {@link UnitsRepresentationUtils#TOP} as the top */
        @Override
        protected Set<AnnotationMirror> findTops(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> tops = AnnotationUtils.createAnnotationSet();

            tops.add(unitsRepUtils.TOP);

            // remove RAWUNITSREP in supertypes
            assert supertypes.containsKey(unitsRepUtils.RAWUNITSREP);
            supertypes.remove(unitsRepUtils.RAWUNITSREP);
            // add TOP to supertypes
            supertypes.put(unitsRepUtils.TOP, Collections.emptySet());

            return tops;
        }

        /** Programmatically set {@link UnitsRepresentationUtils#BOTTOM} as the bottom */
        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> bottoms = AnnotationUtils.createAnnotationSet();

            bottoms.add(unitsRepUtils.BOTTOM);

            // set direct supertypes of BOTTOM and add to supertypes
            Set<AnnotationMirror> bottomSupers = new LinkedHashSet<>();
            bottomSupers.add(unitsRepUtils.POLYUNIT);
            bottomSupers.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.BOTTOM, Collections.unmodifiableSet(bottomSupers));

            return bottoms;
        }

        /**
         * Programmatically set {@link UnitsRepresentationUtils#POLYUNIT} as the polymorphic
         * qualifiers
         */
        @Override
        protected void addPolyRelations(
                QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops,
                Set<AnnotationMirror> bottoms) {

            // polyQualifiers {null=@PolyAll, @UnitsRep=@PolyUnit}
            // replace RAWUNITSREP -> @PolyUnit with TOP -> @PolyUnit
            // Build up a replacement map by looping through polyQualifiers as it is a simple hash
            // map. The null key causes crashes if not handled correctly.
            Map<AnnotationMirror, AnnotationMirror> updatedPolyQualifiers = new HashMap<>();
            for (Entry<AnnotationMirror, AnnotationMirror> e : polyQualifiers.entrySet()) {
                if (AnnotationUtils.areSame(e.getKey(), unitsRepUtils.RAWUNITSREP)) {
                    updatedPolyQualifiers.put(unitsRepUtils.TOP, e.getValue());
                } else {
                    updatedPolyQualifiers.put(e.getKey(), e.getValue());
                }
            }
            polyQualifiers.clear();
            polyQualifiers.putAll(updatedPolyQualifiers);

            // add @PolyUnit -> {TOP} to supertypes
            Set<AnnotationMirror> polyUnitSupers = AnnotationUtils.createAnnotationSet();
            polyUnitSupers.add(unitsRepUtils.TOP);
            supertypes.put(unitsRepUtils.POLYUNIT, Collections.unmodifiableSet(polyUnitSupers));

            // System.err.println(" POST ");
            // System.err.println(" supertypes {");
            // for (Entry<?, ?> e : supertypes.entrySet()) {
            // System.err.println(" " + e.getKey() + " -> " + e.getValue());
            // }
            // System.err.println(" }");
            // System.err.println(" polyQualifiers " + polyQualifiers);
            // System.err.println(" tops " + tops);
            // System.err.println(" bottoms " + bottoms);
            // System.err.println();
        }

        @Override
        public boolean isSubtype(AnnotationMirror subAnno, AnnotationMirror superAnno) {
            // System.err.println(" === checking SUBTYPE \n "
            // + getAnnotationFormatter().formatAnnotationMirror(subAnno) + " <:\n"
            // + getAnnotationFormatter().formatAnnotationMirror(superAnno) + "\n");

            // replace raw @UnitsRep with Dimensionless
            // for some reason this shows up in inference mode when building the lattice
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.RAWUNITSREP)) {
                return isSubtype(unitsRepUtils.DIMENSIONLESS, superAnno);
            }
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.RAWUNITSREP)) {
                return isSubtype(subAnno, unitsRepUtils.DIMENSIONLESS);
            }

            // Case: All units <: Top
            if (AnnotationUtils.areSame(superAnno, unitsRepUtils.TOP)) {
                return true;
            }
            // Case: Bottom <: All units
            if (AnnotationUtils.areSame(subAnno, unitsRepUtils.BOTTOM)) {
                return true;
            }

            // Case: @UnitsRep(x) <: @UnitsRep(y)
            if (AnnotationUtils.areSameByClass(subAnno, UnitsRep.class)
                    && AnnotationUtils.areSameByClass(superAnno, UnitsRep.class)) {

                return AnnotationUtils.areSame(subAnno, superAnno);
                // return UnitsTypecheckUtils.unitsEqual(subAnno, superAnno);

                // if (AnnotationUtils.areSame(superAnno, unitsRepUtils.METER)) {
                // System.err.println(" === checking SUBTYPE \n "
                // + getAnnotationFormatter().formatAnnotationMirror(subAnno) + " <:\n"
                // + getAnnotationFormatter().formatAnnotationMirror(superAnno) + "\n"
                // + " result: " + result);
                // }

                // return result;
            }

            return true;
        }
    }

    @Override
    protected ViewpointAdapter createViewpointAdapter() {
        return checker.hasOption(UnitsChecker.DISABLE_RDU)
                ? super.createViewpointAdapter()
                : new UnitsViewpointAdapter(this);
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new UnitsLiteralTreeAnnotator(), new UnitsPropagationTreeAnnotator());
    }

    protected final class UnitsLiteralTreeAnnotator extends LiteralTreeAnnotator {
        // Programmatically set the qualifier implicits
        public UnitsLiteralTreeAnnotator() {
            super(UnitsAnnotatedTypeFactory.this);
            // set BOTTOM as the literal qualifier for null literals
            addLiteralKind(LiteralKind.NULL, unitsRepUtils.BOTTOM);
            addLiteralKind(LiteralKind.STRING, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.CHAR, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.BOOLEAN, unitsRepUtils.DIMENSIONLESS);
            // in type checking mode, we also set dimensionless for the number literals
            addLiteralKind(LiteralKind.INT, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.LONG, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.FLOAT, unitsRepUtils.DIMENSIONLESS);
            addLiteralKind(LiteralKind.DOUBLE, unitsRepUtils.DIMENSIONLESS);
        }
    }

    private final class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {
        public UnitsPropagationTreeAnnotator() {
            super(UnitsAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitBinary(BinaryTree binaryTree, AnnotatedTypeMirror type) {
            Kind kind = binaryTree.getKind();
            AnnotatedTypeMirror lhsATM = atypeFactory.getAnnotatedType(binaryTree.getLeftOperand());
            AnnotatedTypeMirror rhsATM =
                    atypeFactory.getAnnotatedType(binaryTree.getRightOperand());
            AnnotationMirror lhsAM = lhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);
            AnnotationMirror rhsAM = rhsATM.getEffectiveAnnotationInHierarchy(unitsRepUtils.TOP);

            switch (kind) {
                case PLUS:
                    // if it is a string concatenation, result is dimensionless
                    if (TreeUtils.isStringConcatenation(binaryTree)) {
                        type.replaceAnnotation(unitsRepUtils.DIMENSIONLESS);
                    } else {
                        type.replaceAnnotation(
                                atypeFactory.getQualifierHierarchy().leastUpperBound(lhsAM, rhsAM));
                    }
                    //
                    // else if (AnnotationUtils.areSame(lhsAM, rhsAM)) {
                    // type.replaceAnnotation(lhsAM);
                    // } else {
                    // type.replaceAnnotation(unitsRepUtils.TOP);
                    // }
                    break;
                case MINUS:
                    // if (AnnotationUtils.areSame(lhsAM, rhsAM)) {
                    // type.replaceAnnotation(lhsAM);
                    // } else {
                    // type.replaceAnnotation(unitsRepUtils.TOP);
                    // }
                    type.replaceAnnotation(
                            atypeFactory.getQualifierHierarchy().leastUpperBound(lhsAM, rhsAM));
                    break;
                case MULTIPLY:
                    type.replaceAnnotation(UnitsTypecheckUtils.multiplication(lhsAM, rhsAM));
                    break;
                case DIVIDE:
                    type.replaceAnnotation(UnitsTypecheckUtils.division(lhsAM, rhsAM));
                    break;
                case REMAINDER:
                    type.replaceAnnotation(lhsAM);
                    break;
                case CONDITIONAL_AND: // &&
                case CONDITIONAL_OR: // ||
                case LOGICAL_COMPLEMENT: // !
                case EQUAL_TO: // ==
                case NOT_EQUAL_TO: // !=
                case GREATER_THAN: // >
                case GREATER_THAN_EQUAL: // >=
                case LESS_THAN: // <
                case LESS_THAN_EQUAL: // <=
                    // output of comparisons is a dimensionless binary
                    type.replaceAnnotation(unitsRepUtils.DIMENSIONLESS);
                    break;
                default:
                    // Check LUB by default
                    return super.visitBinary(binaryTree, type);
            }

            return null;
        }
    }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(
                new UnitsDefaultForTypeAnnotator(this), super.createTypeAnnotator());
    }

    protected class UnitsDefaultForTypeAnnotator extends DefaultForTypeAnnotator {
        // Programmatically set the qualifier
        public UnitsDefaultForTypeAnnotator(AnnotatedTypeFactory atf) {
            super(atf);

            // add defaults for exceptions
            addTypes(java.lang.Exception.class, unitsRepUtils.DIMENSIONLESS);
            addTypes(java.lang.Throwable.class, unitsRepUtils.DIMENSIONLESS);
            addTypes(java.lang.Void.class, unitsRepUtils.BOTTOM);
        }
    }

    // for use in AnnotatedTypeMirror.toString()
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new DefaultAnnotatedTypeFormatter(
                createAnnotationFormatter(),
                checker.hasOption("printVerboseGenerics"),
                checker.hasOption("printAllQualifiers"));
    }

    // for use in generating error outputs
    @Override
    protected AnnotationFormatter createAnnotationFormatter() {
        return new UnitsAnnotationFormatter(checker);
    }
}
