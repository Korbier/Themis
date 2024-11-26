package org.sc.themis.renderer.exception;

import org.sc.themis.shared.exception.ThemisException;

public class UnmappedMemoryException extends ThemisException {

    public UnmappedMemoryException( long memoryHandle ) {
        super( "Memory " + memoryHandle + " is unmapped" );
    }

}
