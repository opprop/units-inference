import org.checkerframework.framework.qual.PolyAll;
import units.qual.*;
import units.UnitsTools;

class jBlasCases {
    void system() {
        long savedTime = System.currentTimeMillis();
        double seconds = (double) (System.currentTimeMillis() - savedTime) / 1e3;

        double t = System.nanoTime();
        // :: fixable-error: (argument.type.incompatible)
        System.out.printf("%.1fs\n", (System.nanoTime() - t) / 1e9);
    }

    double sinc(double x) {
        if (x == 0)
            return 1.0;
        else
            // :: fixable-error: (argument.type.incompatible)
            return Math.sin(Math.PI * x) / (Math.PI * x);
    }
}

class Timer {
    long startTime;
    long stopTime;

    long stop() {
        // :: fixable-error: (assignment.type.incompatible)
        startTime = System.nanoTime();
        // :: fixable-error: (assignment.type.incompatible)
        stopTime = System.nanoTime();
        return stopTime - startTime;
    }

    boolean ranFor(double seconds) {
        // :: fixable-error: (comparison.unit.mismatch) :: fixable-error: (subtraction.unit.mismatch)
        return (System.nanoTime() - startTime) / 1e9 >= seconds;
    }

    double elapsedSeconds() {
        return (stopTime - startTime) / 1e9;
    }
}

