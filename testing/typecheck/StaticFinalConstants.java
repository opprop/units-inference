import static java.lang.Math.*;

import units.qual.*;

class StaticFinalConstants {

    void inferRadians() {
        // :: error: (argument.type.incompatible)
        Math.sin(Math.E);

        // :: error: (argument.type.incompatible)
        Math.cos(Math.PI);

        // :: error: (argument.type.incompatible)
        sin(E);

        // :: error: (argument.type.incompatible)
        cos(PI);
    }
}
