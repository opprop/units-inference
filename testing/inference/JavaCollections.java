import units.UnitsTools;
import units.qual.*;

import java.util.*;

class JavaCollections {

    void collections() {
        // infers that the list has to be a list of meters
        List<Integer> x = new ArrayList<>();

        List<@m Integer> y = new ArrayList<>();

        @m Integer meterOne = new @m Integer(5 * UnitsTools.m);

        // :: fixable-warning: (cast.unsafe.constructor.invocation)
        @m Integer meterTwo = new @m Integer(5);

        // :: fixable-error: (argument.type.incompatible)
        x.add(meterTwo);

        Integer meterOut = x.iterator().next();

        // :: fixable-error: (assignment.type.incompatible)
        @m Integer meterOutUpperBound = meterOut;
    }
}
