package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class WrongRendererTypeException extends ThemisException {

    public WrongRendererTypeException( Class<?> provided ) {
        super( "Wrong renderer type is provided : " + provided.getCanonicalName() );
    }

}
