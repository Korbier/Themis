package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class SurfaceFormatNotFoundException extends ThemisException {

    public SurfaceFormatNotFoundException() {
        super( "Surface format not found" );
    }

}

