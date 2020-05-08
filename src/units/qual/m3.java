package units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A cubic meter.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsAlias(baseUnitComponents = {@BUC(unit = "m", exponent = 3)})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface m3 {}
