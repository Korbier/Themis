package org.sc.playground;

import org.sc.themis.engine.Engine;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;

public class Bootstrap {

  private static final org.jboss.logging.Logger LOG =
      org.jboss.logging.Logger.getLogger(Bootstrap.class);

  Configuration configuration;

  public int run(String... args) throws ThemisException {

    Configuration configuration = new Configuration();

    Playgrounds playground = selectPlayground(args);

    LOG.infof("Running %s playground ...", playground);

    Engine engine =
        new Engine(this.configuration, playground.rendererActivityFactory.apply(configuration));
    engine.setup();
    engine.setGamestate(playground.gamestate);
    engine.run();

    return 0;
  }

  private Playgrounds selectPlayground(String[] args) {

    if (args.length > 0) {
      return Playgrounds.fromName(args[0]);
    }

    return Playgrounds.NOOP;
  }
}
