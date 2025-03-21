package org.sc.viewer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sc.themis.engine.Engine;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class ViewerTest {

  Configuration configuration = Configuration.defaultConfiguration();

  @Test
  @Disabled
  void runViewer() throws ThemisException {

    // Given
    ViewerContext context = ViewerContext.createDefault();
    ViewerGamestate gamestate = new ViewerGamestate(context);
    Engine engine =
        new Engine(
            configuration, new ViewerRendererActivity(this.configuration, context, gamestate));

    // When
    engine.setup();
    engine.setGamestate(gamestate);
    engine.run();

    // Then

    // Cleanup
    engine.cleanup();
  }
}
