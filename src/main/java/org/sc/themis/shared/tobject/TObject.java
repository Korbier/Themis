package org.sc.themis.shared.tobject;

import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public abstract class TObject {

    private final Configuration configuration;

    public TObject(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    abstract public void setup() throws ThemisException;
    abstract public void cleanup() throws ThemisException;

}
