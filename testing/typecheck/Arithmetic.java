import units.UnitsTools;
import units.qual.*;

class Arithmetic {
    void test() {
        @m2 int area1 = UnitsTools.m * UnitsTools.m;
        @m double area2 = Math.pow(UnitsTools.m, 2);
    }
}
