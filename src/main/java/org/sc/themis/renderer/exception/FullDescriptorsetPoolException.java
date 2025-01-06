package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class FullDescriptorsetPoolException extends ThemisException {

    public FullDescriptorsetPoolException() {
        super( "Descriptorset pool is full. Create a new one." );
    }

}
