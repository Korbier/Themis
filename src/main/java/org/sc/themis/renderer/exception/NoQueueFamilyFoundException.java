package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class NoQueueFamilyFoundException extends ThemisException {

    public NoQueueFamilyFoundException() {
        super( "No queue family found." );
    }

}
