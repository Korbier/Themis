package org.sc.playground;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.sc.themis.engine.Engine;
import org.sc.themis.engine.EngineDescriptor;
import org.sc.themis.renderer.RendererDescriptor;
import org.sc.themis.scene.SceneDescriptor;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.WindowDescriptor;

@QuarkusMain
public class Bootstrap implements QuarkusApplication {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(Bootstrap.class);

    @Inject Configuration configuration;

    @Override
    public int run(String ... args) throws ThemisException {

        Playgrounds playground = selectPlayground( args );

        LOG.infof( "Running %s playground ...", playground );

        EngineDescriptor engineDescriptor = new EngineDescriptor(
            new WindowDescriptor( configuration.window().width(), configuration.window().height(), configuration.application().name() ),
            new RendererDescriptor( playground.rendererActivity ),
            new SceneDescriptor()
        );

        Engine engine = new Engine( engineDescriptor );
        engine.setup();
        engine.setGamestate( playground.gamestate );
        engine.run();

        return 0;

    }

    private Playgrounds selectPlayground(String[] args) {

        if ( args.length > 0 ) {
            return Playgrounds.fromName( args[0] );
        }

        return Playgrounds.STARTER;

    }

}
