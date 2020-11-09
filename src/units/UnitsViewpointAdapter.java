package units;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.checkerframework.framework.type.AbstractViewpointAdapter;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;
import units.representation.UnitsRepresentationUtils;

public class UnitsViewpointAdapter extends AbstractViewpointAdapter {

    // static reference to the singleton instance
    protected static UnitsRepresentationUtils unitsRepUtils;

    public UnitsViewpointAdapter(AnnotatedTypeFactory atypeFactory) {
        super(atypeFactory);
        unitsRepUtils = UnitsRepresentationUtils.getInstance();
    }

    @Override
    protected boolean shouldAdaptMember(AnnotatedTypeMirror type, Element element) {
        return false;
    }

    @Override
    protected AnnotationMirror extractAnnotationMirror(AnnotatedTypeMirror atm) {
        return atm.getAnnotationInHierarchy(unitsRepUtils.TOP);
    }

    @Override
    protected AnnotationMirror combineAnnotationWithAnnotation(
            AnnotationMirror receiverAnnotation, AnnotationMirror declaredAnnotation) {
        if (AnnotationUtils.areSame(declaredAnnotation, unitsRepUtils.RECEIVER_DEPENDANT_UNIT)) {
            return receiverAnnotation;
        }
        return declaredAnnotation;
    }
}
