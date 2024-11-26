package org.sc.themis.shared.utils;

public class MathUtils {

    private MathUtils() {}

    public static double log2(int n) {
        return java.lang.Math.log(n) / java.lang.Math.log(2);
    }

}
