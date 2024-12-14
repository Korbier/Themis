package org.sc.themis.shared.utils;

public class MemorySizeUtils {

    public final static int PUSHCONSTANT = 64;

    public final static int FLOAT = 4;
    public final static int INT   = 4;
    public final static int LONG   = 8;
    public final static int VEC2F  = 2 * FLOAT;
    public final static int VEC2I  = 2 * INT;
    public final static int VEC3F  = 3 * FLOAT;
    public final static int VEC4F  = 4 * FLOAT;
    public final static int MAT4x4F = 4 * 4 * FLOAT;

    private MemorySizeUtils() {}

}
