package org.sc.playground.noop;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.RendererActivity;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class NoopRendererActivity extends RendererActivity {

  public NoopRendererActivity(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void setup(Renderer renderer) throws ThemisException {}

  @Override
  public void render(Scene scene, long tpf) throws ThemisException {}

  @Override
  public void resize(Scene scene) throws ThemisException {}

  @Override
  public void cleanup() throws ThemisException {}
}
