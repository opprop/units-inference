package units;

import units.notusedquals.*;
import units.qual.*;

@SuppressWarnings("units")
public class UnitsTools {
    // Static Constants
    public static final @Dimensionless int dimensionless = 1;

    public static final @g int g = 1;
    public static final @kg int kg = 1;

    public static final @mm int mm = 1;
    public static final @m int m = 1;
    public static final @km int km = 1;
    public static final @m2 int m2 = 1;
    public static final @m3 int m3 = 1;

    public static final @s int s = 1;
    public static final @ms int ms = 1;
    public static final @us int us = 1;
    public static final @ns int ns = 1;

    public static final @rad int rad = 1;
    public static final @deg int deg = 1;

    public static final @Pa int Pa = 1;
    public static final @kPa int kPa = 1;
    public static final @bar int bar = 1;

    public static final @K int K = 1;
    public static final @C int C = 1;
    public static final @mol int mol = 1;
    public static final @kmol int kmol = 1;

    public static final @mPERs int mPERs = 1;
    public static final @m3PERhr int m3PERhr = 1;
    public static final @gPERmol int gPERmol = 1;

    public static final @W int W = 1;
    public static final @N int N = 1;
    public static final @J int J = 1;
    public static final @MJ int MJ = 1;

    public static final @hr int hr = 1;
    public static final @Dimensionless int day = 1;
    public static final @Dimensionless int year = 1;

    // Conversion
    public static @kg double g_to_kg(@g double g) {
        return g / 1000;
    }

    // Conversion
    public static @kg double g8_to_kg(@g8 double g8) {
        return g8 / 100000;
    }

    public static @kgPERmol double gPERmol_to_kgPERmol(@gPERmol double gPERmol) {
        return gPERmol / 1000;
    }

    public static @bar double pa_to_bar(@Pa double pa) {
        return pa / 100;
    }

    public static @Pa double bar_to_pa(@bar double bar) {
        return bar * 100;
    }

    public static @m double km_to_m(@km double km) {
        return km * 1000;
    }

    public static @m double mm_to_m(@mm double mm) {
        return mm / 1000;
    }

    public static @mm double m_to_mm(@m double m) {
        return m * 1000;
    }

    public static @K double C_to_K(@C double C) {
        return C + 273.15;
    }

    public static @ms double s_to_ms(@s double s) {
        return s * 1000;
    }

    // Constants
    public static final @mPERs2 double gravity = 9.80665;
    public static final @JPERKmol double R = 8.31446261815324 * (J / K) / mol;

    // Testing use only
    public static final @UnknownUnits int top = 1;
    public static final @UnitsBottom int bottom = 1;
    public static final @Dimensionless int one = 1;
    public static final @Dimensionless int Dimensionless = 1;

    // Conversion Functions
    public static final @ms int secondsToMilliSeconds(@s int seconds) {
        return seconds * 1000;
    }
}
