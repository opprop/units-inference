package units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * UnitReceiverDependant is the top type of the type hierarchy.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@Documented
@SubtypeOf(UnitsRep.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER}) // ElementType.TYPE,
public @interface RDU {}
