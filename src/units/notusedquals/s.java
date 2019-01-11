package units.notusedquals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import units.qual.BaseUnit;

/**
 * A second (1/60 of a minute).
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@BaseUnit
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface s {}
