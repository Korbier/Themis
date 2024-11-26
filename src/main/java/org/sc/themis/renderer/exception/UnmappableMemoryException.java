package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class UnmappableMemoryException extends ThemisException {

    public UnmappableMemoryException( long memoryHandle ) {
        super( "Memory " + memoryHandle + " is unmappable" );
    }

}
