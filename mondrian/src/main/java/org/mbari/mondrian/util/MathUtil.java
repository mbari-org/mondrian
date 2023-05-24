package org.mbari.mondrian.util;

public class MathUtil {

    private MathUtil() {
        // no instantiation
    }

    public static int doubleToInt(double d) {
        return Math.toIntExact(Math.round(d));
    }
}
