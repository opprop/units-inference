import units.qual.*;

class Casting {
    // :: fixable-error: (assignment.type.incompatible)
    @m int primM2 = (int) 10.0f;
    // :: fixable-error: (assignment.type.incompatible)
    @s Integer primS = 30;

    int primDim = (int) 20.0f;

    int boxDim = 20;
    @m int boxM = (@m int) boxDim;

    void cast() {
        int x = 20;
        @m int y = (@m int) x;

        Integer boxX = 20;
        @m Integer boxY = (@m Integer) boxX;
    }
}
