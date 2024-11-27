package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class UnknownLayerException extends ThemisException {

    public UnknownLayerException( String layername ) {
        super( "Layer " + layername + " is unknown." );
    }

}
