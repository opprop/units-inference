import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.TimeUnit;
import units.qual.*;

class JavaUtilConcurrentTimeUnit {

    @SuppressWarnings("units")
    enum MyTimeUnit {
        NANOSECONDS,
        MICROSECONDS,
        @ms
        MILLISECONDS,
        @s SECONDS
    }

    void readFromEnumInSource() {
        @s MyTimeUnit u1 = MyTimeUnit.SECONDS;
        @ms MyTimeUnit u2 = MyTimeUnit.MILLISECONDS;

        MyTimeUnit u3 = MyTimeUnit.MICROSECONDS;
        MyTimeUnit u4 = MyTimeUnit.NANOSECONDS;
    }

    void test(long time, TimeUnit unit) throws Exception {
        // :: error: (argument.type.incompatible)
        @ms long milliseconds = unit.toMillis(time);
        Thread.sleep(milliseconds);
    }

    void test2() {
        @s TimeUnit s = TimeUnit.SECONDS;
        @ms TimeUnit ms = TimeUnit.MILLISECONDS;
        @us TimeUnit us = TimeUnit.MICROSECONDS;
        @ns TimeUnit ns = NANOSECONDS;

        // :: error: (assignment.type.incompatible)
        ms = ns;

        // ensure RDU convert works
        @s long seconds = s.convert(10L, NANOSECONDS);
        // :: error: (assignment.type.incompatible)
        @s long not_seconds = ms.convert(10L, NANOSECONDS);

        @s long secondsTwo = TimeUnit.SECONDS.convert(10L, NANOSECONDS);
        // :: error: (assignment.type.incompatible)
        @s long not_secondsTwo = TimeUnit.MILLISECONDS.convert(10L, NANOSECONDS);

        // convert 10 minutes to milliseconds
        @ms long milliseconds = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);

        @us long microsec = TimeUnit.MICROSECONDS.convert(10L, TimeUnit.HOURS);

        @ns long nanosec = NANOSECONDS.convert(10L, TimeUnit.DAYS);
    }

    void bad() {
        // :: error: (assignment.type.incompatible)
        @ns TimeUnit s1 = TimeUnit.MICROSECONDS;
        // :: error: (assignment.type.incompatible)
        @us TimeUnit s2 = TimeUnit.MILLISECONDS;
        // :: error: (assignment.type.incompatible)
        @ms TimeUnit s3 = TimeUnit.SECONDS;
        // :: error: (assignment.type.incompatible)
        @s TimeUnit s4 = NANOSECONDS;
    }

    void testParams(@ns long ns, @s long s) {
        @s TimeUnit s_unit = TimeUnit.SECONDS;
        @ns TimeUnit ns_unit = NANOSECONDS;

        TimeUnit.SECONDS.toMillis(s);
        TimeUnit.NANOSECONDS.toMillis(ns);
        // :: error: (argument.type.incompatible)
        TimeUnit.NANOSECONDS.toMillis(s);
        // :: error: (argument.type.incompatible)
        TimeUnit.SECONDS.toMillis(ns);

        ns_unit.toMillis(ns);
        s_unit.toMillis(s);
        // :: error: (argument.type.incompatible)
        s_unit.toMillis(ns);
        // :: error: (argument.type.incompatible)
        ns_unit.toMillis(s);
    }
}
