package org.sc.playground;

import org.sc.themis.engine.Engine;
import org.sc.themis.shared.configuration.Configuration;
import org.sc.themis.shared.exception.ThemisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

  private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  Configuration configuration;

  public int run(String... args) throws ThemisException {

    Configuration configuration = new Configuration();

    Playgrounds playground = selectPlayground(args);

    logger.info("Running {} playground ...", playground);

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
