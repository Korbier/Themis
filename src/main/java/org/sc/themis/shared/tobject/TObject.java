package org.sc.themis.shared.tobject;

import org.sc.themis.shared.configuration.Configuration;

public abstract class TObject implements LifeCycle {

  private final Configuration configuration;

  public TObject(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

}
