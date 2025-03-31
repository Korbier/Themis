package org.sc.viewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sc.TestWithConfiguration;
import org.sc.themis.engine.Engine;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class ViewerTest extends TestWithConfiguration {

  @Test
  @Disabled
  void runViewer() throws ThemisException {

    // Given
    ViewerContext context = ViewerContext.createDefault();
    MaterialManager materialManager = new MaterialManager();
    ViewerGamestate gamestate = new ViewerGamestate(context, materialManager);
    Engine engine = new Engine(getConfiguration(), new ViewerRendererActivity(getConfiguration(), context, gamestate, materialManager));

    // When
    engine.setup();
    engine.setGamestate(gamestate);
    engine.run();

    // Then

    // Cleanup
    engine.cleanup();

  }
}
