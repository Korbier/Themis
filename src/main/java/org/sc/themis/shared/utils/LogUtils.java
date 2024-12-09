package org.sc.themis.shared.utils;

public class LogUtils {

    private LogUtils() {}

    public static String toHexString( long value ) {
        return "0x" + Long.toHexString( value);
    }

}
