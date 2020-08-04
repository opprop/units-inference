import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.TimeUnit;
import units.qual.*;

class JavaUtilConcurrentTimeUnit {

    void test(long time, TimeUnit unit) throws Exception {
        // :: fixable-error: (argument.type.incompatible)
        long milliseconds = unit.toMillis(time);
        Thread.sleep(milliseconds);
    }

    void test2() {
        TimeUnit s = TimeUnit.SECONDS;

        TimeUnit ms = TimeUnit.MILLISECONDS;

        TimeUnit us = TimeUnit.MICROSECONDS;

        TimeUnit ns = NANOSECONDS;

        @s long seconds = s.convert(10L, NANOSECONDS);

        // convert 10 minutes to milliseconds
        @ms long milliseconds = ms.convert(10L, TimeUnit.MINUTES);

        @us long microsec = us.convert(10L, TimeUnit.HOURS);

        @ns long nanosec = ns.convert(10L, TimeUnit.DAYS);
    }

    void testParams() {
        TimeUnit s_unit = TimeUnit.SECONDS;
        TimeUnit ns_unit = NANOSECONDS;

        long ns = 1000;
        long s = 1000;

        // :: fixable-error: (argument.type.incompatible)
        TimeUnit.SECONDS.toMillis(s);
        // :: fixable-error: (argument.type.incompatible)
        TimeUnit.NANOSECONDS.toMillis(ns);

        // :: fixable-error: (argument.type.incompatible)
        ns_unit.toMillis(ns);
        // :: fixable-error: (argument.type.incompatible)
        s_unit.toMillis(s);
    }

    void testReturn(long s1, long s2) {
        TimeUnit s = TimeUnit.SECONDS;
        TimeUnit ms = TimeUnit.MILLISECONDS;

        // :: fixable-error: (assignment.type.incompatible)
        s1 = s.convert(10L, NANOSECONDS);
        // :: fixable-error: (assignment.type.incompatible)
        s2 = TimeUnit.SECONDS.convert(10L, NANOSECONDS);
    }
}
