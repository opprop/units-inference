package units;

import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceQualifierHierarchy;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.AnnotationLocation;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.qual.VarAnnot;
import checkers.inference.util.InferenceViewpointAdapter;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotationClassLoader;
import org.checkerframework.framework.type.DefaultAnnotatedTypeFormatter;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.framework.util.AnnotationFormatter;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.UserError;

import units.representation.UnitsRepresentationUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

public class UnitsInferenceAnnotatedTypeFactory extends InferenceAnnotatedTypeFactory {
    // static reference to the singleton instance
    protected static UnitsRepresentationUtils unitsRepUtils;

    public UnitsInferenceAnnotatedTypeFactory(
            InferenceChecker inferenceChecker,
            BaseAnnotatedTypeFactory realTypeFactory,
            InferrableChecker realChecker,
            SlotManager slotManager,
            ConstraintManager constraintManager) {
        super(
                inferenceChecker,
                false,
                realTypeFactory,
                realChecker,
                slotManager,
                constraintManager);

        // Should already be initialized in the real ATF
        unitsRepUtils = UnitsRepresentationUtils.getInstance();
        // and it should already have some base units
        if (unitsRepUtils.baseUnits().isEmpty()) {
            throw new UserError("Must supply at least 1 base unit to use Units Checker");
        }

        // unitsRepUtils.VARANNOT = getVarAnnot();

        // build in the same way as DefaultSlotManager's varannot
        // AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
        // VarAnnot.class);
        // builder.setValue("value", -1);
        // unitsRepUtils.VARANNOT = builder.build();

        postInit();
    }

    @Override
    protected AnnotationClassLoader createAnnotationClassLoader() {
        // Use the Units Annotated Type Loader instead of the default one
        return new UnitsAnnotationClassLoader(checker);
    }

    //    // In Inference ATF, this returns the set of real qualifiers
    //    @Override
    //    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    //        // get all the loaded annotations
    //        Set<Class<? extends Annotation>> qualSet = new HashSet<Class<? extends Annotation>>();
    //        // Super grabs all supported qualifiers from the real qualifier hierarchy
    //        // and also puts in VarAnnot
    //        qualSet.addAll(super.createSupportedTypeQualifiers());
    //
    //        // System.err.println( " --- quals = " + qualSet );
    //
    //        // // load all the external units
    //        // loadAllExternalUnits();
    //        //
    //        // // copy all loaded external Units to qual set
    //        // qualSet.addAll(externalQualsMap.values());
    //
    //        return qualSet;
    //    }

    // In Inference ATF, this returns the alias for a given real qualifier
    @Override
    public AnnotationMirror canonicalAnnotation(AnnotationMirror anno) {
        // TODO: cache results
        AnnotationMirror result = realTypeFactory.canonicalAnnotation(anno);
        // System.err.println(" === Aliasing: " + anno.toString() + " ==> " + result);

        if (result == null) {
            result = super.canonicalAnnotation(anno);
        }
        return result;
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new UnitsInferenceQualifierHierarchy(factory);
    }

    private final class UnitsInferenceQualifierHierarchy extends InferenceQualifierHierarchy {
        public UnitsInferenceQualifierHierarchy(MultiGraphFactory multiGraphFactory) {
            super(multiGraphFactory);
        }

        // In inference mode, the only bottom is VarAnnot
        @Override
        protected Set<AnnotationMirror> findBottoms(
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypes) {
            Set<AnnotationMirror> newBottoms = super.findBottoms(supertypes);
            newBottoms.remove(unitsRepUtils.RAWUNITSREP);
            return newBottoms;
        }

        // In inference mode, the only qualifier is VarAnnot. The poly qualifiers are
        // PolyAll and any poly qual from the type system.
        @Override
        protected void finish(
                QualifierHierarchy qualHierarchy,
                Map<AnnotationMirror, Set<AnnotationMirror>> supertypesMap,
                Map<AnnotationMirror, AnnotationMirror> polyQualifiers,
                Set<AnnotationMirror> tops,
                Set<AnnotationMirror> bottoms,
                Object... args) {
            super.finish(qualHierarchy, supertypesMap, polyQualifiers, tops, bottoms, args);

            // TODO: this update, which is sensible to keep the inference qual hierarchy clean,
            // causes crashes in creating constant slots for @PolyUnit
            // disabling for now

            /*
             * Map before update:
            supertypesMap
              @checkers.inference.qual.VarAnnot -> [@org.checkerframework.framework.qual.PolyAll]
              @org.checkerframework.framework.qual.PolyAll -> [@checkers.inference.qual.VarAnnot, @units.qual.UnitsRep]
              @units.qual.PolyUnit -> [@org.checkerframework.framework.qual.PolyAll, @units.qual.UnitsRep]
              @units.qual.UnitsRep -> []
            polyQualifiers {null=@org.checkerframework.framework.qual.PolyAll, @units.qual.UnitsRep=@units.qual.PolyUnit}
            tops [@checkers.inference.qual.VarAnnot]
            bottoms [@checkers.inference.qual.VarAnnot]
             */
            //
            // // Remove @UnitsRep from super of PolyAll
            // assert supertypesMap.containsKey(unitsRepUtils.POLYALL);
            // Set<AnnotationMirror> polyAllSupers = AnnotationUtils.createAnnotationSet();
            // polyAllSupers.addAll(supertypesMap.get(unitsRepUtils.POLYALL));
            // polyAllSupers.remove(unitsRepUtils.RAWUNITSINTERNAL);
            // supertypesMap.put(unitsRepUtils.POLYALL,
            // Collections.unmodifiableSet(polyAllSupers));
            //
            // // Remove @UnitsRep from super of PolyUnit
            // assert supertypesMap.containsKey(unitsRepUtils.POLYUNIT);
            // Set<AnnotationMirror> polyUnitSupers = AnnotationUtils.createAnnotationSet();
            // polyUnitSupers.addAll(supertypesMap.get(unitsRepUtils.POLYUNIT));
            // polyUnitSupers.remove(unitsRepUtils.RAWUNITSINTERNAL);
            // supertypesMap.put(unitsRepUtils.POLYUNIT,
            // Collections.unmodifiableSet(polyUnitSupers));
            //
            // // Remove @UnitsRep from map
            // supertypesMap.remove(unitsRepUtils.RAWUNITSINTERNAL);
            //
            // // Remove @UnitsRep from polyQualifiers
            // assert polyQualifiers.containsKey(unitsRepUtils.RAWUNITSINTERNAL);
            // polyQualifiers.remove(unitsRepUtils.RAWUNITSINTERNAL);
            //
            // System.err.println(" === Inference ATF ");
            // System.err.println(" supertypesMap ");
            // for (Entry<?, ?> e : supertypesMap.entrySet()) {
            // System.err.println(" " + e.getKey() + " -> " + e.getValue());
            // }
            // System.err.println(" polyQualifiers " + polyQualifiers);
            // System.err.println(" tops " + tops);
            // System.err.println(" bottoms " + bottoms);

            /*
            * Map after update:
            supertypesMap
              @checkers.inference.qual.VarAnnot -> [@org.checkerframework.framework.qual.PolyAll]
              @org.checkerframework.framework.qual.PolyAll -> [@checkers.inference.qual.VarAnnot]
              @units.qual.PolyUnit -> [@org.checkerframework.framework.qual.PolyAll]
            polyQualifiers {null=@org.checkerframework.framework.qual.PolyAll}
            tops [@checkers.inference.qual.VarAnnot]
            bottoms [@checkers.inference.qual.VarAnnot]
            */
        }

        @Override
        public Set<? extends AnnotationMirror> leastUpperBounds(
                Collection<? extends AnnotationMirror> annos1,
                Collection<? extends AnnotationMirror> annos2) {
            if (InferenceMain.isHackMode(annos1.size() != annos2.size())) {
                Set<AnnotationMirror> result = AnnotationUtils.createAnnotationSet();
                for (AnnotationMirror a1 : annos1) {
                    for (AnnotationMirror a2 : annos2) {
                        AnnotationMirror lub = leastUpperBound(a1, a2);
                        if (lub != null) {
                            result.add(lub);
                        }
                    }
                }

                return result;
            }

            return super.leastUpperBounds(annos1, annos2);
        }
    }

    @Override
    protected Set<? extends AnnotationMirror> getDefaultTypeDeclarationBounds() {
        Set<AnnotationMirror> top = new HashSet<>();
        top.add(unitsRepUtils.TOP);
        return top;
    }

    @Override
    protected InferenceViewpointAdapter createViewpointAdapter() {
        return new UnitsInferenceViewpointAdapter(this);
    }

    @Override
    public VariableAnnotator createVariableAnnotator() {
        return new UnitsVariableAnnotator(
                this, realTypeFactory, realChecker, slotManager, constraintManager);
    }

    private final class UnitsVariableAnnotator extends VariableAnnotator {

        public UnitsVariableAnnotator(
                InferenceAnnotatedTypeFactory typeFactory,
                AnnotatedTypeFactory realTypeFactory,
                InferrableChecker realChecker,
                SlotManager slotManager,
                ConstraintManager constraintManager) {
            super(typeFactory, realTypeFactory, realChecker, slotManager, constraintManager);
        }

        @Override
        public void handleBinaryTree(AnnotatedTypeMirror atm, BinaryTree binaryTree) {
            // Super creates an LUB constraint by default, we create an VariableSlot here
            // instead for the result of the binary op and create LUB constraint in units
            // visitor.
            if (treeToVarAnnoPair.containsKey(binaryTree)) {
                atm.replaceAnnotations(treeToVarAnnoPair.get(binaryTree).second);
            } else {
                // grab slots for the component (only for lub slot)
                AnnotatedTypeMirror lhsATM =
                        inferenceTypeFactory.getAnnotatedType(binaryTree.getLeftOperand());
                AnnotatedTypeMirror rhsATM =
                        inferenceTypeFactory.getAnnotatedType(binaryTree.getRightOperand());
                VariableSlot lhs = slotManager.getVariableSlot(lhsATM);
                VariableSlot rhs = slotManager.getVariableSlot(rhsATM);

                // create varslot for the result of the binary tree computation
                // note: constraints for binary ops are added in UnitsVisitor
                VariableSlot result;
                switch (binaryTree.getKind()) {
                    case PLUS:
                        // if it is a string concatenation, result is dimensionless
                        if (TreeUtils.isStringConcatenation(binaryTree)) {
                            result = slotManager.createConstantSlot(unitsRepUtils.DIMENSIONLESS);
                            break;
                        } // else create arithmetic slot
                    case MINUS:
                    case MULTIPLY:
                    case DIVIDE:
                    case REMAINDER:
                        result =
                                slotManager.createArithmeticVariableSlot(
                                        VariableAnnotator.treeToLocation(
                                                inferenceTypeFactory, binaryTree));
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
                        result = slotManager.createConstantSlot(unitsRepUtils.DIMENSIONLESS);
                        break;
                    default:
                        result = slotManager.createLubVariableSlot(lhs, rhs);
                        break;
                }

                // insert varAnnot of the slot into the ATM
                AnnotationMirror resultAM = slotManager.getAnnotation(result);
                atm.clearAnnotations();
                atm.replaceAnnotation(resultAM);

                // add to cache
                Set<AnnotationMirror> resultSet = AnnotationUtils.createAnnotationSet();
                resultSet.add(resultAM);
                final Pair<VariableSlot, Set<? extends AnnotationMirror>> varATMPair =
                        Pair.of(slotManager.getVariableSlot(atm), resultSet);
                treeToVarAnnoPair.put(binaryTree, varATMPair);
            }
        }
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new UnitsInferenceTreeAnnotator(
                        this, realChecker, realTypeFactory, variableAnnotator, slotManager));
    }

    private final class UnitsInferenceTreeAnnotator extends InferenceTreeAnnotator {
        // TODO: per design of InferenceTreeAnnotator, this code should be moved into
        // UnitsVariableAnnotator if it performs deep traversal

        public UnitsInferenceTreeAnnotator(
                InferenceAnnotatedTypeFactory atypeFactory,
                InferrableChecker realChecker,
                AnnotatedTypeFactory realAnnotatedTypeFactory,
                VariableAnnotator variableAnnotator,
                SlotManager slotManager) {
            super(
                    atypeFactory,
                    realChecker,
                    realAnnotatedTypeFactory,
                    variableAnnotator,
                    slotManager);
        }

        // ====== generate variable slots for static final constants such as Math.PI and Math.E

        // For when the constants are directly used via static import: "PI"
        @Override
        public Void visitIdentifier(IdentifierTree tree, AnnotatedTypeMirror atm) {
            super.visitIdentifier(tree, atm);
            generateVarSlotForStaticFinalConstants(tree, tree.getName(), atm);
            return null;
        }

        // For when the constants are used via Class.Constant: "Math.PI"
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, AnnotatedTypeMirror atm) {
            super.visitMemberSelect(tree, atm);
            generateVarSlotForStaticFinalConstants(tree, tree.getIdentifier(), atm);
            return null;
        }

        // Generate a variable slot for a static final constant only if it
        // isn't already annotated in source or in a stub with a non-dimensionless unit
        private void generateVarSlotForStaticFinalConstants(
                Tree tree, Name name, AnnotatedTypeMirror atm) {
            // The element being accessed: F in "X.F" or "F" depending on static import
            Element member = TreeUtils.elementFromTree(tree);
            // // The class declaring element: X in "X.F" or "F" depending on static import
            // TypeElement declaringClass = ElementUtils.enclosingClass(member);
            // boolean isUnitsToolsConstant = TypesUtils.isDeclaredOfName(
            // declaringClass.asType(), UnitsTools.class.getCanonicalName());

            // the member must be a static final program constant
            if (ElementUtils.isStatic(member)
                    && ElementUtils.isFinal(member)
                    && ElementUtils.isCompileTimeConstant(member)) {

                AnnotationMirror am = atm.getEffectiveAnnotationInHierarchy(getVarAnnot());
                if (am != null) {
                    Slot slot = slotManager.getSlot(am);
                    boolean fromByteCode = ElementUtils.isElementFromByteCode(member);

                    // System.err.println(" member: " + member);
                    // System.err.println(" am: " + am);
                    // System.err.println(" fromByteCode: " + fromByteCode);
                    // System.err.println(" slot.isVariable(): " + slot.isVariable());
                    // System.err.println(" isDimensionless: " + AnnotationUtils.areSame(
                    // ((ConstantSlot) slot).getValue(), unitsRepUtils.DIMENSIONLESS));

                    // if member is from source code, it must be unannotated
                    // if member is from byte code, it must not be annotated with a
                    // non-dimensionless unit
                    if ((!fromByteCode && slot.isVariable())
                            || (fromByteCode
                                    && slot.isConstant()
                                    && AnnotationUtils.areSame(
                                            ((ConstantSlot) slot).getValue(),
                                            unitsRepUtils.DIMENSIONLESS))) {
                        // Generate a fresh variable for inference
                        AnnotationLocation loc =
                                VariableAnnotator.treeToLocation(atypeFactory, tree);
                        VariableSlot varSlot = slotManager.createVariableSlot(loc);
                        atm.replaceAnnotation(slotManager.getAnnotation(varSlot));
                    }
                }
            }
        }

        // ====== handle polymorphic returns in constructors and methods

        // see if given annotation mirror is the VarAnnot versions of @PolyUnit and
        // @PolyAll
        private boolean isPolyAnnotation(AnnotationMirror annot) {
            Slot slot = slotManager.getSlot(annot);
            if (slot.isConstant()) {
                AnnotationMirror constant = ((ConstantSlot) slot).getValue();
                return InferenceQualifierHierarchy.isPolymorphic(constant);
            }
            return false;
        }

        // based on InferenceATF.constructorFromUse()
        private boolean isConstructorDeclaredWithPolymorphicReturn(NewClassTree newClassTree) {
            final ExecutableElement constructorElem = TreeUtils.constructor(newClassTree);
            final AnnotatedTypeMirror constructorReturnType = fromNewClass(newClassTree);
            final AnnotatedExecutableType constructorType =
                    AnnotatedTypes.asMemberOf(
                            types,
                            UnitsInferenceAnnotatedTypeFactory.this,
                            constructorReturnType,
                            constructorElem);

            // if any of the annotations on the return type of the constructor is a
            // polymorphic annotation, return true
            for (AnnotationMirror annot : constructorType.getReturnType().getAnnotations()) {
                if (isPolyAnnotation(annot)) {
                    return true;
                }
            }

            return false;
        }

        // based on InferenceATF.methodFromUse()
        private boolean isMethodDeclaredWithPolymorphicReturn(
                MethodInvocationTree methodInvocationTree) {
            final ExecutableElement methodElem = TreeUtils.elementFromUse(methodInvocationTree);
            // final AnnotatedExecutableType methodType = getAnnotatedType(methodElem);

            final ExpressionTree methodSelectExpression = methodInvocationTree.getMethodSelect();
            final AnnotatedTypeMirror receiverType;
            if (methodSelectExpression.getKind() == Tree.Kind.MEMBER_SELECT) {
                receiverType =
                        getAnnotatedType(
                                ((MemberSelectTree) methodSelectExpression).getExpression());
            } else {
                receiverType = getSelfType(methodInvocationTree);
            }

            final AnnotatedExecutableType methodOfReceiver =
                    AnnotatedTypes.asMemberOf(
                            types,
                            UnitsInferenceAnnotatedTypeFactory.this,
                            receiverType,
                            methodElem);

            // if any of the annotations on the return type of the constructor is a
            // polymorphic annotation, return true
            for (AnnotationMirror annot : methodOfReceiver.getReturnType().getAnnotations()) {
                if (isPolyAnnotation(annot)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Void visitNewClass(NewClassTree newClassTree, AnnotatedTypeMirror atm) {
            // Call super to replace polymorphic annotations with fresh variable slots
            super.visitNewClass(newClassTree, atm);

            /*
             * given   @m Clazz x = new Clazz(param)   where the constructor is
             * polymorphic: @Poly Clazz(@Poly arg)
             *
             * without the fix below, the constraints are:
             *
             * @1 <: @3
             * @2 <: @3
             * @1 <: @m
             *
             * inserted as @m Clazz x = new @1 Clazz(@2 param)
             * @3 is not inserted
             *
             * this isn't sufficient, as there is no requirement that @2 <: @m
             * in turn, it fails in type checking as the LUB of @1 and @2 can be a supertype of @m
             *
             * with the fix below, the constraints are:
             *
             * @1 <: @3
             * @2 <: @3
             * @3 <: @m
             *
             * inserted as @m Clazz x = new @1 Clazz(@2 param)
             */

            if (isConstructorDeclaredWithPolymorphicReturn(newClassTree)) {
                // For a call "new @m Clazz(@m arg)" on a polymorphic constructor
                // "@Poly Clazz(@Poly param)" we have the following annotations:

                // 1) the variable slot generated for the polymorphic declared return type
                VariableSlot varSlotForPolyReturn =
                        variableAnnotator.getOrCreatePolyVar(newClassTree);
                // disable insertion of polymorphic return variable slot
                varSlotForPolyReturn.setInsertable(false);

                // 2) the call site return type: "@m" in "new @m Clazz(...)"
                VariableSlot callSiteReturnVarSlot = slotManager.getVariableSlot(atm);

                // Create a subtype constraint: callSiteReturnVarSlot <: varSlotForPolyReturn
                // since after annotation insertion, the varSlotForPolyReturn is conceptually a
                // cast of the newly created object:
                // "(@varSlotForPolyReturn Clazz) new @m Clazz(...)"
                constraintManager.addSubtypeConstraint(callSiteReturnVarSlot, varSlotForPolyReturn);

                // Replace the slot/annotation in the atm (callSiteReturnVarSlot) with the
                // varSlotForPolyReturn for upstream analysis
                atm.replaceAnnotation(slotManager.getAnnotation(varSlotForPolyReturn));
            }

            return null;
        }

        @Override
        public Void visitMethodInvocation(
                MethodInvocationTree methodInvocationTree, AnnotatedTypeMirror atm) {
            super.visitMethodInvocation(methodInvocationTree, atm);

            if (isMethodDeclaredWithPolymorphicReturn(methodInvocationTree)) {
                VariableSlot varSlotForPolyReturn =
                        variableAnnotator.getOrCreatePolyVar(methodInvocationTree);
                // disable insertion of polymorphic return variable slot
                varSlotForPolyReturn.setInsertable(false);
            }

            return null;
        }

        @Override
        public Void visitLiteral(final LiteralTree tree, AnnotatedTypeMirror type) {
            switch (tree.getKind()) {
                case NULL_LITERAL:
                    replaceATM(type, unitsRepUtils.BOTTOM);
                    return null;
                case CHAR_LITERAL:
                    replaceATM(type, unitsRepUtils.DIMENSIONLESS);
                    return null;
                case BOOLEAN_LITERAL:
                    replaceATM(type, unitsRepUtils.DIMENSIONLESS);
                    return null;
                case STRING_LITERAL:
                    replaceATM(type, unitsRepUtils.DIMENSIONLESS);
                    return null;
                default:
                    return super.visitLiteral(tree, type);
            }
        }

        private void replaceATM(AnnotatedTypeMirror atm, AnnotationMirror am) {
            final ConstantSlot cs = slotManager.createConstantSlot(am);
            AnnotationBuilder ab =
                    new AnnotationBuilder(realTypeFactory.getProcessingEnv(), VarAnnot.class);
            ab.setValue("value", cs.getId());
            atm.clearAnnotations();
            atm.replaceAnnotation(ab.build());
        }
    }

    // for use in AnnotatedTypeMirror.toString()
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new DefaultAnnotatedTypeFormatter(
                new UnitsAnnotationFormatter(checker),
                checker.hasOption("printVerboseGenerics"),
                checker.hasOption("printAllQualifiers"));
    }

    // for use in generating error outputs
    @Override
    protected AnnotationFormatter createAnnotationFormatter() {
        return new UnitsAnnotationFormatter(checker);
    }
}
