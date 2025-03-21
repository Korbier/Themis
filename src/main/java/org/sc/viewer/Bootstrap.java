package org.sc.viewer;

import org.sc.themis.engine.Engine;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class Bootstrap {

  Configuration configuration;

  public static void main(String[] args) throws ThemisException {
    new Bootstrap().run();
  }

  private void run() throws ThemisException {

    ViewerContext context = ViewerContext.createDefault();

    ViewerGamestate gamestate = new ViewerGamestate(context);
    ViewerRendererActivity activity =
        new ViewerRendererActivity(this.configuration, context, gamestate);

    Engine engine = new Engine(this.configuration, activity);
    engine.setup();

    engine.setGamestate(gamestate);
    engine.run();

  }

}
