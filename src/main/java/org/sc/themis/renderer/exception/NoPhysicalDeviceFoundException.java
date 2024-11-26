package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class NoPhysicalDeviceFoundException extends ThemisException {

    public NoPhysicalDeviceFoundException() {
        super( "No physical device found." );
    }

}
