package org.sc.themis.shared;

import org.sc.themis.shared.exception.ThemisException;

public abstract class TObject<D> {

    private final D descriptor;

    public TObject( D descriptor ) {
        this.descriptor = descriptor;
    }

    public D getDescriptor() {
        return this.descriptor;
    }

    abstract public void setup() throws ThemisException;
    abstract public void cleanup() throws ThemisException;

}
