package units.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bar divide by acceleration.
 *
 * @checker_framework.manual #units-checker Units Checker
 */
@UnitsAlias(
        prefixExponent = 8,
        baseUnitComponents = {
            @BUC(unit = "g", exponent = 1),
            @BUC(unit = "m", exponent = -2),
            @BUC(unit = "s", exponent = 1)
        })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface bars3PERm {}
