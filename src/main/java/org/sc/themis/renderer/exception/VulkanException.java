package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class VulkanException extends ThemisException {

    final private int code;

    public VulkanException(int code, String message) {
        super( code + " = " + message);
        this.code = code;
    }

    public VulkanException( String message, Throwable cause ) {
        super( message, cause );
        this.code = -1;
    }
}
