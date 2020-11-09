package units;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.util.InferenceViewpointAdapter;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;
import units.representation.UnitsRepresentationUtils;

public class UnitsInferenceViewpointAdapter extends InferenceViewpointAdapter {

    // static reference to the singleton instance
    protected static UnitsRepresentationUtils unitsRepUtils;

    public UnitsInferenceViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);
        unitsRepUtils = UnitsRepresentationUtils.getInstance();
    }

    @Override
    protected boolean shouldAdaptMember(AnnotatedTypeMirror type, Element element) {
        return false;
    }

    @Override
    protected AnnotationMirror combineAnnotationWithAnnotation(
            AnnotationMirror receiverAnnotation, AnnotationMirror declaredAnnotation) {
        if (InferenceMain.isHackMode(declaredAnnotation == null)) {
            return unitsRepUtils.BOTTOM;
        }
        Slot declSlot = InferenceMain.getInstance().getSlotManager().getSlot(declaredAnnotation);
        if (declSlot.isConstant()) {
            ConstantSlot cs = (ConstantSlot) declSlot;
            if (AnnotationUtils.areSame(cs.getValue(), unitsRepUtils.RECEIVER_DEPENDANT_UNIT)) {
                return super.combineAnnotationWithAnnotation(
                        receiverAnnotation, declaredAnnotation);
            }
        }
        return declaredAnnotation;
    }
}
