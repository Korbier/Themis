package org.sc.themis.renderer;

import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.core.LifeCycle;

public abstract class RendererActivity implements LifeCycle {

  public abstract void setup(Renderer renderer) throws ThemisException;

  public abstract void render(Scene scene, long tpf) throws ThemisException;

  public abstract void resize(Scene scene) throws ThemisException;

  public void setup(Scene scene) throws ThemisException {}

  @Override
  public void setup() throws ThemisException {}

}
