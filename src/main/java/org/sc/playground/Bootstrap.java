package org.sc.playground;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.sc.themis.engine.Engine;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.exception.ThemisException;

@QuarkusMain
public class Bootstrap implements QuarkusApplication {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(Bootstrap.class);

    @Inject Configuration configuration;

    @Override
    public int run(String ... args) throws ThemisException {

        Playgrounds playground = selectPlayground( args );
        LOG.infof( "Running %s playground ...", playground );

        Engine engine = new Engine( this.configuration, playground.rendererActivityFactory.apply( configuration ) );
        engine.setup();
        engine.setGamestate( playground.gamestate );
        engine.run();

        return 0;

    }

    private Playgrounds selectPlayground(String[] args) {

        if ( args.length > 0 ) {
            return Playgrounds.fromName( args[0] );
        }

        return Playgrounds.NOOP;

    }

}
