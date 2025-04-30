package org.sc.themis.core;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class CoreDiContext implements AutoCloseable {

  private WeldContainer container;

  public CoreDiContext start() {
    Weld weld = new Weld();
    this.container = weld.initialize();
    return this;
  }

  @Override
  public void close() {
    this.container.close();
  }

  public <C> C select(Class<C> type) {
    return this.container.select(type).get();
  }

}
