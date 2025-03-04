package org.sc.viewer;

import io.quarkus.runtime.QuarkusApplication;
import jakarta.inject.Inject;
import org.sc.themis.engine.Engine;
import org.sc.themis.shared.Configuration;
import org.sc.viewer.gamestate.ViewerGamestate;
import org.sc.viewer.renderactivity.ViewerRendererActivity;

public class Bootstrap implements QuarkusApplication {

    @Inject
    Configuration configuration;

    @Override
    public int run(String... args) throws Exception {

        ViewerContext context = new ViewerContext();

        ViewerGamestate gamestate = new ViewerGamestate(context);
        ViewerRendererActivity activity = new ViewerRendererActivity(this.configuration, context, gamestate);

        Engine engine = new Engine(this.configuration, activity);
        engine.setup();

        engine.setGamestate(gamestate);
        engine.run();

        return 0;

    }

}
