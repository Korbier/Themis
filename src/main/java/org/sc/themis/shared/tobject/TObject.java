package org.sc.themis.shared.tobject;

import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public abstract class TObject {

  private final Configuration configuration;

  public TObject(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  public abstract void setup() throws ThemisException;

  public abstract void cleanup() throws ThemisException;
}
