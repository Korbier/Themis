package org.sc.viewer;

import org.sc.themis.engine.Engine;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class Bootstrap {

  public static void main(String[] args) throws ThemisException {
    new Bootstrap().run();
  }

  private void run() throws ThemisException {

    Configuration configuration = new Configuration("./src/main/resources/application.properties");
    ViewerContext context = ViewerContext.createDefault();

    ViewerGamestate gamestate = new ViewerGamestate(context);
    ViewerRendererActivity activity = new ViewerRendererActivity(configuration, context, gamestate);

    Engine engine = new Engine(configuration, activity);
    engine.setup();

    engine.setGamestate(gamestate);
    engine.run();

  }

}
