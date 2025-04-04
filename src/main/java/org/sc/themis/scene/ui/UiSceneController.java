package org.sc.themis.scene.ui;

import org.sc.themis.input.Input;
import org.sc.themis.renderer.pencil2d.Pencil2D;
import org.sc.themis.scene.Scene;
import org.sc.themis.scene.base.Controller;
import org.sc.themis.scene.pencil.Pencil;

public class UiSceneController implements Controller {

  private final Scene scene;
  private final UiBuilder builder;

  public UiSceneController(Scene scene, Pencil2D pencil) {
    this.scene = scene;
    this.builder = new UiBuilder(pencil);
  }

  protected Scene scene() {
    return this.scene;
  }

  protected UiBuilder builder() {
    return this.builder;
  }

  protected void build(long tpf) {
    //Do nothing
  }

  @Override
  final public void update(long tpf) {
    builder.begin();
    build(tpf);
    builder.end();
  }

  @Override
  final public void input(Input input, long tpf) {
    builder.input(input);
  }

}
