package org.sc.themis.shared.utils;

import java.util.Arrays;

public class ArrayUtils {

    private ArrayUtils() {}

    public static <O> O [] merge( O object, O ... others) {
        O [] copy = Arrays.copyOf( others, others.length + 1 );
        copy[0] = object;
        System.arraycopy( others, 0, copy, 1, others.length );
        return copy;
    }

}
