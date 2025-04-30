package org.sc.themis.shared.service;

import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

import java.util.HashMap;
import java.util.Map;

public class ServiceContainer implements LifeCycle {

  private Map<Class<? extends Service>, Service> services = new HashMap<>();

  public <S extends Service> void set(Class<S> type, S serviceImplementation) throws ThemisException {
    serviceImplementation.setup();
    this.services.put(type, serviceImplementation);
  }

  public <S extends Service> S get(Class<S> type) {
    return (S) this.services.get(type);
  }

  public <S> boolean contains(Class<S> type) {
    return this.services.containsKey(type);
  }

  @Override
  public void setup() throws ThemisException {}

  @Override
  public void cleanup() throws ThemisException {
    for (Service service : this.services.values()) {
      service.cleanup();
    }
  }
}
