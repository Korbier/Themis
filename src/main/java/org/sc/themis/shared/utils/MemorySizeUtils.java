package org.sc.themis.shared.utils;

/**
 * Utils class.
 */
public class MemorySizeUtils {

    public static final int PUSHCONSTANT = 64;

    public static final int FLOAT = 4;
    public static final int INT   = 4;
    public static final int LONG   = 8;
    public static final int VEC2F  = 2 * FLOAT;
    public static final int VEC2I  = 2 * INT;
    public static final int VEC3F  = 3 * FLOAT;
    public static final int VEC4F  = 4 * FLOAT;
    public static final int VEC4I  = 4 * INT;
    public static final int MAT4x4F = 4 * 4 * FLOAT;

    private MemorySizeUtils() {}

}
