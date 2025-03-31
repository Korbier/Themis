package org.sc.viewer;

import org.sc.themis.engine.Engine;
import org.sc.themis.renderer.material.MaterialManager;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.renderer.resource.ResourceLoader;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class Bootstrap {

  public static void main(String[] args) throws ThemisException {
    new Bootstrap().run();
  }

  private void run() throws ThemisException {

    Configuration configuration = new Configuration("./application.properties");
    ResourceLoader.get().apply(configuration);

    ViewerContext context = ViewerContext.createDefault();
    MaterialManager mManager = new MaterialManager();

    ViewerGamestate gamestate = new ViewerGamestate(context, mManager);
    ViewerRendererActivity activity = new ViewerRendererActivity(configuration, context, gamestate, mManager);

    Engine engine = new Engine(configuration, activity);
    engine.setup();

    engine.setGamestate(gamestate);
    engine.run();

  }

}
